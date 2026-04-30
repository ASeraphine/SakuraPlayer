#!/bin/zsh

# Sakura Player macOS App Builder
echo "Building Sakura Player macOS Application Bundle..."

# Store the base directory
BASE_DIR="$PWD"

# Clean previous builds
rm -rf dist build input SakuraPlayer.jar
mkdir -p dist input

# Compile Java sources
echo "Compiling source code..."
javac --module-path "lib" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -cp "lib/jaudiotagger-3.0.1.jar:lib/batik-all-1.19.jar:lib/svg-salamander-1.1.5.3.jar:lib/jlayer-1.0.1.jar:lib/mp3agic-0.9.0.jar:src" src/*.java -d bin

# Create executable JAR with Class-Path manifest for non-JavaFX dependencies
echo "Creating JAR file..."
cd bin
echo "Class-Path: jaudiotagger-3.0.1.jar batik-all-1.19.jar svg-salamander-1.1.5.3.jar jlayer-1.0.1.jar mp3agic-0.9.0.jar" > "$BASE_DIR/manifest.txt"
jar cvfm "$BASE_DIR/SakuraPlayer.jar" "$BASE_DIR/manifest.txt" *.class
cd "$BASE_DIR"
rm manifest.txt

# Bundle resources into the JAR
echo "Bundling resources..."
cd res
jar uf "$BASE_DIR/SakuraPlayer.jar" .
cd "$BASE_DIR"

# Copy the JAR and non-JavaFX dependency JARs into the input directory
echo "Preparing input directory for jpackage..."
cp SakuraPlayer.jar input/
cp lib/jaudiotagger-3.0.1.jar input/
cp lib/batik-all-1.19.jar input/
cp lib/svg-salamander-1.1.5.3.jar input/
cp lib/jlayer-1.0.1.jar input/
cp lib/mp3agic-0.9.0.jar input/

# Create .app bundle with jpackage
echo "Creating macOS .app bundle..."
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
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --java-options "-Duser.dir=\${APPDIR}" \
  --module-path "lib" \
  --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web \
  --dest "$BASE_DIR/dist"

# Cleanup
rm SakuraPlayer.jar
rm -rf input

echo ""
echo "✅ Build complete!"
echo "Application created at: dist/Sakura Player.app"
echo ""
echo "You can now drag this to your Applications folder."
