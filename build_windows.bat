@echo off
REM Sakura Player Windows App Builder
echo Building Sakura Player Windows Application...

REM Store the base directory
set BASE_DIR=%CD%

REM Clean previous builds
if exist dist rmdir /s /q dist
if exist build rmdir /s /q build
if exist input rmdir /s /q input
if exist javafx-modules rmdir /s /q javafx-modules
if exist SakuraPlayer.jar del SakuraPlayer.jar
mkdir dist
mkdir input
mkdir javafx-modules

REM Copy only the JavaFX module JARs (exclude JDK internal modules that cause hash conflicts)
echo Preparing JavaFX module path...
copy lib\javafx.base.jar javafx-modules\
copy lib\javafx.controls.jar javafx-modules\
copy lib\javafx.graphics.jar javafx-modules\
copy lib\javafx.media.jar javafx-modules\
copy lib\javafx.swing.jar javafx-modules\
copy lib\javafx.fxml.jar javafx-modules\
copy lib\javafx.web.jar javafx-modules\

REM Compile Java sources
echo Compiling source code...
javac --module-path "javafx-modules" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -cp "lib\jaudiotagger-3.0.1.jar;lib\batik-all-1.19.jar;lib\svg-salamander-1.1.5.3.jar;lib\jlayer-1.0.1.jar;lib\mp3agic-0.9.0.jar;src" src\*.java -d bin

REM Create executable JAR with Class-Path manifest for non-JavaFX dependencies
echo Creating JAR file...
cd bin
echo Class-Path: jaudiotagger-3.0.1.jar batik-all-1.19.jar svg-salamander-1.1.5.3.jar jlayer-1.0.1.jar mp3agic-0.9.0.jar > "%BASE_DIR%\manifest.txt"
jar cvfm "%BASE_DIR%\SakuraPlayer.jar" "%BASE_DIR%\manifest.txt" *.class
cd "%BASE_DIR%"
del manifest.txt

REM Bundle resources into the JAR
echo Bundling resources...
cd res
jar uf "%BASE_DIR%\SakuraPlayer.jar" .
cd "%BASE_DIR%"

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
jpackage --type exe --input "input" --main-jar SakuraPlayer.jar --main-class App --name "SakuraPlayer" --icon "%BASE_DIR%\res\icon.ico" --app-version 1.0 --vendor "SakuraPlayer" --win-dir-chooser --win-menu --win-shortcut --java-options "-Duser.dir=%APPDIR%" --module-path "%BASE_DIR%\javafx-modules" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web --dest "%BASE_DIR%\dist"

REM Cleanup
if exist SakuraPlayer.jar del SakuraPlayer.jar
if exist input rmdir /s /q input
if exist javafx-modules rmdir /s /q javafx-modules

echo.
echo Build complete!
echo Application created at: dist\SakuraPlayer.exe
echo.
pause
