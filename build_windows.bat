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
javac --module-path "lib" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -cp "lib\jaudiotagger-3.0.1.jar;lib\batik-all-1.19.jar;lib\svg-salamander-1.1.5.3.jar;lib\jlayer-1.0.1.jar;lib\mp3agic-0.9.0.jar;src" src\*.java -d bin

REM Create executable JAR
echo Creating JAR file...
cd bin
jar cvfe ..\SakuraPlayer.jar App *
cd ..

REM Bundle resources into the JAR
echo Bundling resources...
cd res
jar uf ..\SakuraPlayer.jar .
cd ..

REM Create .exe bundle with jpackage
echo Creating Windows .exe bundle...
jpackage --type exe --input "%CD%" --main-jar SakuraPlayer.jar --main-class App --name "SakuraPlayer" --icon "%CD%\res\icon.ico" --app-version 1.0 --vendor "SakuraPlayer" --win-dir-chooser --win-menu --win-shortcut --java-options "--enable-native-access=ALL-UNNAMED" --java-options "-Duser.dir=%%APPDIR%%" --module-path "lib" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web --dest "%CD%\dist"

REM Cleanup
if exist SakuraPlayer.jar del SakuraPlayer.jar

echo.
echo Build complete!
echo Application created at: dist\SakuraPlayer.exe
echo.
pause
