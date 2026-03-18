#!/usr/bin/env python3
"""
COMET Release Branch Validation — Local Server
Serves the HTML app and proxies API calls to JIRA/GitHub to avoid CORS issues.
"""

import http.server
import json
import os
import ssl
import sys
import urllib.request
import urllib.error
import urllib.parse
import webbrowser
from http.server import HTTPServer, SimpleHTTPRequestHandler

PORT = 8090

class ProxyHandler(SimpleHTTPRequestHandler):
    """Serves static files and proxies /api/jira/* and /api/github/* requests."""

    def do_OPTIONS(self):
        """Handle CORS preflight."""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept')
        self.send_header('Access-Control-Max-Age', '86400')
        self.end_headers()

    def do_GET(self):
        if self.path.startswith('/api/jira/'):
            self.proxy_request('https://jira.capgroup.com', '/api/jira/')
        elif self.path.startswith('/api/github/'):
            self.proxy_request('https://api.github.com', '/api/github/')
        else:
            # Serve static files
            super().do_GET()

    def do_POST(self):
        if self.path.startswith('/api/jira/'):
            self.proxy_request('https://jira.capgroup.com', '/api/jira/')
        elif self.path.startswith('/api/github/'):
            self.proxy_request('https://api.github.com', '/api/github/')
        else:
            self.send_error(404)

    def proxy_request(self, target_base, prefix):
        """Forward request to target API server."""
        # Build target URL
        path = self.path[len(prefix):]
        target_url = target_base + '/' + path

        # Read request body if present
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length) if content_length > 0 else None

        # Forward headers (only the important ones)
        headers = {}
        for header in ['Authorization', 'Content-Type', 'Accept']:
            value = self.headers.get(header)
            if value:
                headers[header] = value

        # If no Accept header, default to JSON
        if 'Accept' not in headers:
            headers['Accept'] = 'application/json'

        try:
            req = urllib.request.Request(
                target_url,
                data=body,
                headers=headers,
                method=self.command
            )

            # Handle SSL (some corporate environments have custom certs)
            ctx = ssl.create_default_context()
            try:
                response = urllib.request.urlopen(req, context=ctx, timeout=30)
            except ssl.SSLError:
                # Fallback: skip SSL verification for internal servers
                ctx = ssl.create_default_context()
                ctx.check_hostname = False
                ctx.verify_mode = ssl.CERT_NONE
                response = urllib.request.urlopen(req, context=ctx, timeout=30)

            # Send response back to browser
            self.send_response(response.status)
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-Type', response.headers.get('Content-Type', 'application/json'))
            self.end_headers()

            # Stream response body
            data = response.read()
            self.wfile.write(data)

        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            error_body = e.read() if e.fp else b'{}'
            self.wfile.write(error_body)

        except Exception as e:
            self.send_response(502)
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({'error': str(e)}).encode())

    def log_message(self, format, *args):
        """Custom log format."""
        if '/api/' in (args[0] if args else ''):
            # Log API proxy requests
            sys.stdout.write(f"  [PROXY] {args[0]}\n")
            sys.stdout.flush()
        # Suppress static file logs to reduce noise


def main():
    # Change to script directory so it serves files from the right place
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)

    print()
    print("  ╔══════════════════════════════════════════════════╗")
    print("  ║   COMET Release Branch Validation               ║")
    print("  ║   Local server with JIRA/GitHub proxy            ║")
    print("  ╚══════════════════════════════════════════════════╝")
    print()
    print(f"  Server running at:  http://localhost:{PORT}")
    print(f"  Open this URL:      http://localhost:{PORT}/release-gate.html")
    print()
    print("  API proxy routes:")
    print(f"    /api/jira/*    →  jira.capgroup.com")
    print(f"    /api/github/*  →  api.github.com")
    print()
    print("  Press Ctrl+C to stop the server.")
    print()

    server = HTTPServer(('localhost', PORT), ProxyHandler)

    # Auto-open browser
    try:
        webbrowser.open(f'http://localhost:{PORT}/release-gate.html')
    except Exception:
        pass

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n  Server stopped.")
        server.server_close()


if __name__ == '__main__':
    main()
