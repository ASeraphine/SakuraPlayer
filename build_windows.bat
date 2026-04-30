@echo off
REM Sakura Player Windows App Builder
echo Building Sakura Player Windows Application...

REM Clean previous builds
if exist dist rmdir /s /q dist
if exist build rmdir /s /q build
if exist SakuraPlayer.jar del SakuraPlayer.jar
mkdir dist

REM Compile Java sources
echo Compiling source code...
javac -cp "lib/*;src" src/*.java -d bin

REM Create executable JAR
echo Creating JAR file...
cd bin
jar cvfe ..\SakuraPlayer.jar App *
cd ..

REM Bundle resources and libraries into the JAR
echo Bundling resources and libraries...
cd res
jar uf ..\SakuraPlayer.jar .
cd ..
cd lib
jar uf ..\SakuraPlayer.jar .
cd ..

REM Create .exe bundle with jpackage
echo Creating Windows .exe bundle...
jpackage ^
  --type exe ^
  --input "%CD%" ^
  --main-jar SakuraPlayer.jar ^
  --main-class App ^
  --name "Sakura Player" ^
  --icon "%CD%\res\icon.ico" ^
  --app-version 1.0 ^
  --vendor "Sakura Player" ^
  --copyright "Copyright 2026" ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --java-options "-Duser.dir=%APPDIR%" ^
  --dest "%CD%\dist"

REM Cleanup
if exist SakuraPlayer.jar del SakuraPlayer.jar

echo.
echo Build complete!
echo Application created at: dist\Sakura Player.exe
echo.
pause
