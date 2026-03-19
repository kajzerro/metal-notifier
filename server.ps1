<#
  COMET Release Branch Validation — Local Server
  PowerShell proxy: serves HTML + forwards API calls to JIRA/GitHub (no CORS issues)
  No dependencies required — runs on any Windows PC.
#>

$Port = 8090
$Prefix = "http://localhost:$Port/"

# Get script directory to serve files from
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $ScriptDir) { $ScriptDir = Get-Location }

Write-Host ""
Write-Host "  ================================================" -ForegroundColor Cyan
Write-Host "   COMET Release Branch Validation" -ForegroundColor White
Write-Host "   Local server with JIRA/GitHub proxy" -ForegroundColor Gray
Write-Host "  ================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Server running at:  " -NoNewline
Write-Host "http://localhost:$Port" -ForegroundColor Green
Write-Host "  Opening browser..." -ForegroundColor Gray
Write-Host ""
Write-Host "  API proxy routes:"
Write-Host "    /api/jira/*    ->  capitalgroup.atlassian.net" -ForegroundColor DarkGray
Write-Host "    /api/github/*  ->  api.github.com" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  Press Ctrl+C to stop." -ForegroundColor Yellow
Write-Host ""

# Allow self-signed / corporate certs
Add-Type @"
using System.Net;
using System.Security.Cryptography.X509Certificates;
public class TrustAll : ICertificatePolicy {
    public bool CheckValidationResult(ServicePoint sp, X509Certificate cert, WebRequest req, int problem) { return true; }
}
"@
[System.Net.ServicePointManager]::CertificatePolicy = New-Object TrustAll
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12

# MIME types
$MimeTypes = @{
    ".html" = "text/html; charset=utf-8"
    ".js"   = "application/javascript"
    ".css"  = "text/css"
    ".json" = "application/json"
    ".png"  = "image/png"
    ".ico"  = "image/x-icon"
    ".svg"  = "image/svg+xml"
}

function Get-MimeType($path) {
    $ext = [System.IO.Path]::GetExtension($path).ToLower()
    if ($MimeTypes.ContainsKey($ext)) { return $MimeTypes[$ext] }
    return "application/octet-stream"
}

function Send-ProxyRequest($context, $targetBase, $prefixToStrip) {
    $req = $context.Request
    $resp = $context.Response

    # Build target URL
    $localPath = $req.Url.PathAndQuery
    $stripped = $localPath.Substring($prefixToStrip.Length)
    $targetUrl = "$targetBase/$stripped"

    try {
        $webReq = [System.Net.HttpWebRequest]::Create($targetUrl)
        $webReq.Method = $req.HttpMethod
        $webReq.Timeout = 30000
        $webReq.UserAgent = "COMET-ReleaseGate/1.0"

        # Forward key headers
        foreach ($header in @("Authorization", "Accept")) {
            $val = $req.Headers[$header]
            if ($val) {
                if ($header -eq "Authorization") {
                    $webReq.Headers["Authorization"] = $val
                } elseif ($header -eq "Accept") {
                    $webReq.Accept = $val
                }
            }
        }
        $webReq.ContentType = "application/json"

        # Forward body for POST/PUT
        if ($req.HttpMethod -in @("POST", "PUT") -and $req.ContentLength64 -gt 0) {
            $body = New-Object byte[] $req.ContentLength64
            $offset = 0
            while ($offset -lt $body.Length) {
                $read = $req.InputStream.Read($body, $offset, $body.Length - $offset)
                if ($read -eq 0) { break }
                $offset += $read
            }
            $webReq.ContentLength = $body.Length
            $reqStream = $webReq.GetRequestStream()
            $reqStream.Write($body, 0, $body.Length)
            $reqStream.Close()
        }

        # Get response
        $webResp = $webReq.GetResponse()
        $respStream = $webResp.GetResponseStream()
        $reader = New-Object System.IO.MemoryStream
        $respStream.CopyTo($reader)
        $respBytes = $reader.ToArray()
        $reader.Close()
        $respStream.Close()

        $resp.StatusCode = [int]$webResp.StatusCode
        $resp.ContentType = $webResp.ContentType
        $resp.AddHeader("Access-Control-Allow-Origin", "*")
        $resp.OutputStream.Write($respBytes, 0, $respBytes.Length)
        $webResp.Close()

        Write-Host "  [PROXY] $($req.HttpMethod) $stripped -> $([int]$webResp.StatusCode)" -ForegroundColor DarkGray
    }
    catch [System.Net.WebException] {
        $errResp = $_.Exception.Response
        if ($errResp) {
            $errStream = $errResp.GetResponseStream()
            $errReader = New-Object System.IO.StreamReader($errStream)
            $errBody = $errReader.ReadToEnd()
            $errReader.Close()

            $resp.StatusCode = [int]$errResp.StatusCode
            $resp.ContentType = "application/json"
            $resp.AddHeader("Access-Control-Allow-Origin", "*")
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($errBody)
            $resp.OutputStream.Write($bytes, 0, $bytes.Length)

            Write-Host "  [PROXY] $($req.HttpMethod) $stripped -> $([int]$errResp.StatusCode)" -ForegroundColor Red
        } else {
            $resp.StatusCode = 502
            $resp.ContentType = "application/json"
            $resp.AddHeader("Access-Control-Allow-Origin", "*")
            $errMsg = "{`"error`": `"$($_.Exception.Message)`"}"
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($errMsg)
            $resp.OutputStream.Write($bytes, 0, $bytes.Length)

            Write-Host "  [PROXY] $($req.HttpMethod) $stripped -> 502 $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    catch {
        $resp.StatusCode = 502
        $resp.ContentType = "application/json"
        $resp.AddHeader("Access-Control-Allow-Origin", "*")
        $errMsg = "{`"error`": `"$($_.Exception.Message)`"}"
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($errMsg)
        $resp.OutputStream.Write($bytes, 0, $bytes.Length)

        Write-Host "  [PROXY] ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
    finally {
        $resp.Close()
    }
}

function Send-StaticFile($context, $filePath) {
    $resp = $context.Response
    $fullPath = Join-Path $ScriptDir $filePath

    if (-not (Test-Path $fullPath)) {
        $resp.StatusCode = 404
        $resp.Close()
        return
    }

    $bytes = [System.IO.File]::ReadAllBytes($fullPath)
    $resp.ContentType = Get-MimeType $fullPath
    $resp.ContentLength64 = $bytes.Length
    $resp.AddHeader("Access-Control-Allow-Origin", "*")
    $resp.OutputStream.Write($bytes, 0, $bytes.Length)
    $resp.Close()
}

# Start listener
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add($Prefix)

try {
    $listener.Start()
}
catch {
    Write-Host "  ERROR: Could not start server on port $Port." -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Try closing other applications using port $Port, or run as Administrator." -ForegroundColor Yellow
    Read-Host "  Press Enter to exit"
    exit 1
}

# Open browser
Start-Process "http://localhost:$Port/release-gate.html"

# Main loop
try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $req = $context.Request
        $path = $req.Url.AbsolutePath

        # Handle CORS preflight
        if ($req.HttpMethod -eq "OPTIONS") {
            $context.Response.StatusCode = 200
            $context.Response.AddHeader("Access-Control-Allow-Origin", "*")
            $context.Response.AddHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            $context.Response.AddHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept")
            $context.Response.AddHeader("Access-Control-Max-Age", "86400")
            $context.Response.Close()
            continue
        }

        # Route requests
        if ($path.StartsWith("/api/jira/")) {
            Send-ProxyRequest $context "https://capitalgroup.atlassian.net" "/api/jira/"
        }
        elseif ($path.StartsWith("/api/github/")) {
            Send-ProxyRequest $context "https://api.github.com" "/api/github/"
        }
        elseif ($path -eq "/") {
            # Redirect root to the app
            $context.Response.Redirect("/release-gate.html")
            $context.Response.Close()
        }
        else {
            # Serve static file
            $filePath = $path.TrimStart("/")
            Send-StaticFile $context $filePath
        }
    }
}
catch {
    # Ctrl+C or error
}
finally {
    Write-Host "`n  Server stopped." -ForegroundColor Yellow
    $listener.Stop()
    $listener.Close()
}
