@echo off
setlocal
cd /d "%~dp0"

echo Building the verified local Create Deep Seas checkpoint...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-release.ps1"
set "RESULT=%ERRORLEVEL%"

if not "%RESULT%"=="0" (
    echo.
    echo Build failed. Existing release and dist outputs were not replaced.
) else (
    echo.
    echo Verified local checkpoint completed successfully.
)

echo.
pause
exit /b %RESULT%
