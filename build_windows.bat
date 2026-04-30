@echo off
REM Sakura Player Windows App Builder
echo Building Sakura Player Windows Application...

REM Clean previous builds
if exist dist rmdir /s /q dist
if exist build rmdir /s /q build
if exist input rmdir /s /q input
if exist SakuraPlayer.jar del SakuraPlayer.jar
mkdir dist
mkdir input

REM Compile Java sources
echo Compiling source code...
javac --module-path "lib" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -cp "lib\jaudiotagger-3.0.1.jar;lib\batik-all-1.19.jar;lib\svg-salamander-1.1.5.3.jar;lib\jlayer-1.0.1.jar;lib\mp3agic-0.9.0.jar;src" src\*.java -d bin

REM Create executable JAR with Class-Path manifest for non-JavaFX dependencies
echo Creating JAR file...
cd bin
echo Class-Path: jaudiotagger-3.0.1.jar batik-all-1.19.jar svg-salamander-1.1.5.3.jar jlayer-1.0.1.jar mp3agic-0.9.0.jar > ..\manifest.txt
jar cvfm ..\SakuraPlayer.jar ..\manifest.txt App *
cd ..
del manifest.txt

REM Bundle resources into the JAR
echo Bundling resources...
cd res
jar uf ..\SakuraPlayer.jar .
cd ..

REM Copy the JAR and non-JavaFX dependency JARs into the input directory
echo Preparing input directory for jpackage...
copy SakuraPlayer.jar input\
copy lib\jaudiotagger-3.0.1.jar input\
copy lib\batik-all-1.19.jar input\
copy lib\svg-salamander-1.1.5.3.jar input\
copy lib\jlayer-1.0.1.jar input\
copy lib\mp3agic-0.9.0.jar input\

REM Create .exe bundle with jpackage
echo Creating Windows .exe bundle...
jpackage --type exe --input "input" --main-jar SakuraPlayer.jar --main-class App --name "SakuraPlayer" --icon "%CD%\res\icon.ico" --app-version 1.0 --vendor "SakuraPlayer" --win-dir-chooser --win-menu --win-shortcut --java-options "--enable-native-access=ALL-UNNAMED" --java-options "-Duser.dir=%%APPDIR%%" --module-path "lib" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web --dest "%CD%\dist"

REM Cleanup
if exist SakuraPlayer.jar del SakuraPlayer.jar
if exist input rmdir /s /q input

echo.
echo Build complete!
echo Application created at: dist\SakuraPlayer.exe
echo.
pause
