@echo off
cd /d "%~dp0"
setlocal

REM ==== Adapte ces deux chemins si besoin ====
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
REM ============================================

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Repertoire de travail : %CD%
echo.
echo Java utilise :
"%JAVA_HOME%\bin\java" -version

echo.
echo === Compilation de l'APK (debug) ===
call "%~dp0gradlew.bat" assembleDebug

if errorlevel 1 (
    echo.
    echo Echec de la compilation.
    pause
    exit /b 1
)

echo.
echo === APK genere ===
echo app\build\outputs\apk\debug\app-debug.apk
pause

endlocal
