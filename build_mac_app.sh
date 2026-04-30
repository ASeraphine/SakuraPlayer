#!/bin/zsh

# Sakura Player macOS App Builder
# Builds both macOS .app and Windows .exe (via Launch4j)
echo "============================================"
echo " Sakura Player Build Script"
echo "============================================"
echo ""

# Store the base directory
BASE_DIR="$PWD"

# Clean previous builds
rm -rf dist build input javafx-modules SakuraPlayer.jar
mkdir -p dist input javafx-modules

# Copy only the JavaFX module JARs (exclude JDK internal modules that cause hash conflicts)
echo "[1/6] Preparing JavaFX module path..."
cp lib/javafx.base.jar javafx-modules/
cp lib/javafx.controls.jar javafx-modules/
cp lib/javafx.graphics.jar javafx-modules/
cp lib/javafx.media.jar javafx-modules/
cp lib/javafx.swing.jar javafx-modules/
cp lib/javafx.fxml.jar javafx-modules/
cp lib/javafx.web.jar javafx-modules/

# Compile Java sources
echo "[2/6] Compiling source code..."
javac --module-path "javafx-modules" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -cp "lib/jaudiotagger-3.0.1.jar:lib/batik-all-1.19.jar:lib/svg-salamander-1.1.5.3.jar:lib/jlayer-1.0.1.jar:lib/mp3agic-0.9.0.jar:src" src/*.java -d bin

# Create executable JAR with Class-Path manifest for non-JavaFX dependencies
echo "[3/6] Creating JAR file..."
cd bin
echo "Class-Path: jaudiotagger-3.0.1.jar batik-all-1.19.jar svg-salamander-1.1.5.3.jar jlayer-1.0.1.jar mp3agic-0.9.0.jar" > "$BASE_DIR/manifest.txt"
jar cvfm "$BASE_DIR/SakuraPlayer.jar" "$BASE_DIR/manifest.txt" *.class
cd "$BASE_DIR"
rm manifest.txt

# Bundle resources into the JAR
echo "[4/6] Bundling resources..."
cd res
jar uf "$BASE_DIR/SakuraPlayer.jar" .
cd "$BASE_DIR"

# ============================================
# Build macOS .app bundle
# ============================================
echo "[5/6] Creating macOS .app bundle..."

# Copy the JAR and non-JavaFX dependency JARs into the input directory
cp SakuraPlayer.jar input/
cp lib/jaudiotagger-3.0.1.jar input/
cp lib/batik-all-1.19.jar input/
cp lib/svg-salamander-1.1.5.3.jar input/
cp lib/jlayer-1.0.1.jar input/
cp lib/mp3agic-0.9.0.jar input/

jpackage \
  --type app-image \
  --input "input" \
  --main-jar SakuraPlayer.jar \
  --main-class App \
  --name "Sakura Player" \
  --icon "$BASE_DIR/res/icon.icns" \
  --app-version 1.0 \
  --vendor "Sakura Player" \
  --copyright "Copyright © 2026" \
  --mac-package-name "Sakura Player" \
  --mac-package-identifier com.sakuraplayer.app \
  --java-options "-Duser.dir=\${APPDIR}" \
  --module-path "$BASE_DIR/javafx-modules" \
  --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web \
  --dest "$BASE_DIR/dist"

# ============================================
# Build Windows .exe with Launch4j
# ============================================
echo "[6/6] Creating Windows .exe with Launch4j..."

# Prepare input directory for Launch4j (separate from jpackage input)
rm -rf input
mkdir -p input

cp SakuraPlayer.jar input/
cp lib/jaudiotagger-3.0.1.jar input/
cp lib/batik-all-1.19.jar input/
cp lib/svg-salamander-1.1.5.3.jar input/
cp lib/jlayer-1.0.1.jar input/
cp lib/mp3agic-0.9.0.jar input/
cp lib/javafx.base.jar input/
cp lib/javafx.controls.jar input/
cp lib/javafx.graphics.jar input/
cp lib/javafx.media.jar input/
cp lib/javafx.swing.jar input/
cp lib/javafx.fxml.jar input/
cp lib/javafx.web.jar input/

# Check if launch4j is installed
if command -v launch4j &> /dev/null; then
  launch4j launch4j.xml
  echo "  ✅ Windows .exe created at: dist/SakuraPlayer.exe"
elif [ -f /usr/local/bin/launch4j ]; then
  /usr/local/bin/launch4j launch4j.xml
  echo "  ✅ Windows .exe created at: dist/SakuraPlayer.exe"
else
  echo "  ⚠️  Launch4j not found! Install it with: brew install launch4j"
  echo "     Skipping Windows .exe build."
  echo ""
  echo "     To build the Windows .exe later, run:"
  echo "     brew install launch4j && launch4j launch4j.xml"
fi

# Cleanup
rm SakuraPlayer.jar
rm -rf input javafx-modules

echo ""
echo "============================================"
echo " ✅ Build complete!"
echo "============================================"
echo ""
echo "macOS app:  dist/Sakura Player.app"
if [ -f dist/SakuraPlayer.exe ]; then
  echo "Windows exe: dist/SakuraPlayer.exe"
fi
echo ""
echo "You can now drag the .app to your Applications folder."
