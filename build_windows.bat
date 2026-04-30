@echo off
REM Sakura Player Windows App Builder (using Launch4j)
echo Building Sakura Player Windows Application...

REM Store the base directory
set BASE_DIR=%CD%

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
echo Class-Path: jaudiotagger-3.0.1.jar batik-all-1.19.jar svg-salamander-1.1.5.3.jar jlayer-1.0.1.jar mp3agic-0.9.0.jar > "%BASE_DIR%\manifest.txt"
jar cvfm "%BASE_DIR%\SakuraPlayer.jar" "%BASE_DIR%\manifest.txt" *.class
cd "%BASE_DIR%"
del manifest.txt

REM Bundle resources into the JAR
echo Bundling resources...
cd res
jar uf "%BASE_DIR%\SakuraPlayer.jar" .
cd "%BASE_DIR%"

REM Copy the JAR and all dependency JARs into the input directory
echo Preparing input directory for Launch4j...
copy SakuraPlayer.jar input\
copy lib\jaudiotagger-3.0.1.jar input\
copy lib\batik-all-1.19.jar input\
copy lib\svg-salamander-1.1.5.3.jar input\
copy lib\jlayer-1.0.1.jar input\
copy lib\mp3agic-0.9.0.jar input\
copy lib\javafx.base.jar input\
copy lib\javafx.controls.jar input\
copy lib\javafx.graphics.jar input\
copy lib\javafx.media.jar input\
copy lib\javafx.swing.jar input\
copy lib\javafx.fxml.jar input\
copy lib\javafx.web.jar input\

REM Create .exe with Launch4j
echo Creating Windows .exe with Launch4j...
echo.
echo Make sure Launch4j is installed and launch4jc.exe is in your PATH.
echo Download from: https://sourceforge.net/projects/launch4j/
echo.
launch4jc.exe launch4j.xml

REM If launch4jc is not in PATH, try common install locations
if not exist dist\SakuraPlayer.exe (
  if exist "C:\Program Files\Launch4j\launch4jc.exe" (
    "C:\Program Files\Launch4j\launch4jc.exe" launch4j.xml
  ) else if exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" (
    "C:\Program Files (x86)\Launch4j\launch4jc.exe" launch4j.xml
  ) else (
    echo.
    echo Launch4j not found! Please install it or add it to PATH.
    echo Download: https://sourceforge.net/projects/launch4j/
    echo.
    echo The JAR file has been created at: SakuraPlayer.jar
    echo You can run it manually with:
    echo java --module-path "input" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -jar SakuraPlayer.jar
  )
)

REM Cleanup
if exist SakuraPlayer.jar del SakuraPlayer.jar
if exist input rmdir /s /q input

echo.
echo Build complete!
if exist dist\SakuraPlayer.exe (
  echo Application created at: dist\SakuraPlayer.exe
) else (
  echo JAR file created at: SakuraPlayer.jar
)
echo.
pause
