
@echo off
title COMET Release Branch Validation
echo.
echo  ================================================
echo   COMET Release Branch Validation - Starting...
echo  ================================================
echo.
 
where python >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    python server.py
    goto :end
)
 
where py >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    py -3 server.py
    goto :end
)
 
echo  ERROR: Python 3 is required but was not found.
echo  Install Python from https://www.python.org/downloads/
echo  or via COMET Applications to Install.
echo.
pause
 
:end
