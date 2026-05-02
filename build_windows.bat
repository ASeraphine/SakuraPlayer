@echo off
REM Sakura Player Windows App Builder (using jpackage)
echo Building Sakura Player Windows Application...

REM Store the base directory
set BASE_DIR=%CD%

REM Clean previous builds
if exist dist rmdir /s /q dist
if exist build rmdir /s /q build
if exist input rmdir /s /q input
if exist javafx-modules rmdir /s /q javafx-modules
if exist custom-runtime rmdir /s /q custom-runtime
if exist SakuraPlayer.jar del SakuraPlayer.jar
mkdir dist input javafx-modules

REM Download JavaFX 25 SDK for Windows
echo Downloading JavaFX 25 SDK...
if not exist "%BASE_DIR%\tools\javafx-sdk-25.0.3-win" (
  mkdir "%BASE_DIR%\tools"
  echo   Downloading from GluonHQ...
  powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/25.0.3/openjfx-25.0.3_windows-x64_bin-sdk.zip' -OutFile '%TEMP%\javafx25-win.zip'"
  powershell -Command "Expand-Archive -Path '%TEMP%\javafx25-win.zip' -DestinationPath '%BASE_DIR%\tools' -Force"
  ren "%BASE_DIR%\tools\javafx-sdk-25.0.3" "javafx-sdk-25.0.3-win"
  del "%TEMP%\javafx25-win.zip"
)
set JAVAFX_DIR=%BASE_DIR%\tools\javafx-sdk-25.0.3-win

REM Copy JavaFX 25 module JARs (exclude JDK internal modules that cause hash conflicts)
echo Copying JavaFX modules...
copy "%JAVAFX_DIR%\lib\javafx.base.jar" javafx-modules\
copy "%JAVAFX_DIR%\lib\javafx.controls.jar" javafx-modules\
copy "%JAVAFX_DIR%\lib\javafx.graphics.jar" javafx-modules\
copy "%JAVAFX_DIR%\lib\javafx.media.jar" javafx-modules\
copy "%JAVAFX_DIR%\lib\javafx.swing.jar" javafx-modules\
copy "%JAVAFX_DIR%\lib\javafx.fxml.jar" javafx-modules\
copy "%JAVAFX_DIR%\lib\javafx.web.jar" javafx-modules\

REM Compile Java sources
echo Compiling source code...
javac --module-path "javafx-modules" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web,java.logging -cp "lib\jaudiotagger-3.0.1.jar;lib\jlayer-1.0.1.jar;lib\mp3agic-0.9.0.jar;src" src\*.java -d bin

REM Create executable JAR with Class-Path manifest for non-JavaFX dependencies
echo Creating JAR file...
cd bin
echo Class-Path: jaudiotagger-3.0.1.jar jlayer-1.0.1.jar mp3agic-0.9.0.jar > "%BASE_DIR%\manifest.txt"
jar cvfm "%BASE_DIR%\SakuraPlayer.jar" "%BASE_DIR%\manifest.txt" *.class
cd "%BASE_DIR%"
del manifest.txt

REM Bundle resources into the JAR
echo Bundling resources...
cd res
jar uf "%BASE_DIR%\SakuraPlayer.jar" .
cd "%BASE_DIR%"

REM Create custom jlink runtime with JavaFX modules
echo Creating custom runtime with JavaFX modules...
jlink ^
  --module-path "%JAVAFX_DIR%\lib;%JAVA_HOME%\jmods" ^
  --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web,java.logging,java.desktop,java.naming,java.scripting,jdk.unsupported,jdk.crypto.ec ^
  --output "%BASE_DIR%\custom-runtime" ^
  --no-header-files ^
  --no-man-pages

REM Copy JavaFX native DLLs into the custom runtime
echo Copying JavaFX native libraries into custom runtime...
copy "%JAVAFX_DIR%\bin\decora_sse.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\fxplugins.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\glass.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\glib-lite.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\gstreamer-lite.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\javafx_font.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\javafx_iio.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\jfxmedia.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\jfxwebkit.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\prism_common.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\prism_d3d.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
copy "%JAVAFX_DIR%\bin\prism_sw.dll" "%BASE_DIR%\custom-runtime\bin\" 2>nul
echo Custom runtime created with JavaFX modules

REM Copy the JAR and non-JavaFX dependency JARs into the input directory
copy SakuraPlayer.jar input\
copy lib\jaudiotagger-3.0.1.jar input\
copy lib\jlayer-1.0.1.jar input\
copy lib\mp3agic-0.9.0.jar input\

REM Create .exe with jpackage
echo Creating Windows .exe with jpackage...
jpackage ^
  --type app-image ^
  --runtime-image "%BASE_DIR%\custom-runtime" ^
  --input "input" ^
  --main-jar SakuraPlayer.jar ^
  --main-class App ^
  --name "Sakura Player" ^
  --icon "%BASE_DIR%\res\icon.ico" ^
  --app-version 1.0 ^
  --vendor "Sakura Player" ^
  --copyright "Copyright 2026" ^
  --java-options "-Duser.dir=\\%%APPDIR%%" ^
  --dest "%BASE_DIR%\dist"

REM Cleanup
if exist SakuraPlayer.jar del SakuraPlayer.jar
if exist input rmdir /s /q input
if exist javafx-modules rmdir /s /q javafx-modules

echo.
echo Build complete!
if exist "dist\Sakura Player\SakuraPlayer.exe" (
  echo Application created at: dist\Sakura Player\SakuraPlayer.exe
) else (
  echo Check dist\ directory for the built application
)
echo.
pause
