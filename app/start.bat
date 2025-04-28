@echo off

set SERVICE_NAME=sap
set APP_NAME=sap.exe
set WEB_URL=http://127.0.0.1:10010

for /f "tokens=1,2" %%i in ('jps -l') do (
    echo %%i %%j | find "%SERVICE_NAME%" >nul
    if not errorlevel 1 (
        for /f "tokens=3,4" %%a in ('"reg query HKEY_CLASSES_ROOT\http\shell\open\command"') do (set SoftWareRoot=%%a %%b)
          start "" % SoftWareRoot % %WEB_URL%
    ) else (
        start %~dp0\ %APP_NAME%
    )
)
