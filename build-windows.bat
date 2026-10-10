@echo off
setlocal
set "JAVA_HOME=%APPDATA%\.sklauncher\runtime\java-runtime-delta\windows-x64\java-runtime-delta"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "TEMP=%ProgramData%\OxyRPG\temp"
set "TMP=%TEMP%"
set "GRADLE_USER_HOME=%ProgramData%\OxyRPG\gradle-home"
if not exist "%TEMP%" mkdir "%TEMP%"
set "JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=%TEMP% -Djava.io.tmpdir=%TEMP%"
if not exist "%ProgramData%\OxyRPG\project\gradlew.bat" (
  echo Project junction missing: %ProgramData%\OxyRPG\project
  exit /b 1
)
call "%ProgramData%\OxyRPG\project\gradlew.bat" -p "%ProgramData%\OxyRPG\project" --no-daemon --no-configuration-cache --max-workers=2 assemble %*
exit /b %ERRORLEVEL%

