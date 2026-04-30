#!/bin/zsh

# Sakura Player macOS App Builder
echo "Building Sakura Player macOS Application Bundle..."

# Clean previous builds
rm -rf dist build SakuraPlayer.jar
mkdir -p dist

# Compile Java sources
echo "Compiling source code..."
javac --module-path "lib" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web -cp "lib/jaudiotagger-3.0.1.jar:lib/batik-all-1.19.jar:lib/svg-salamander-1.1.5.3.jar:lib/jlayer-1.0.1.jar:lib/mp3agic-0.9.0.jar:src" src/*.java -d bin

# Create executable JAR
echo "Creating JAR file..."
cd bin
jar cvfe ../SakuraPlayer.jar App *
cd ..

# Bundle resources into the JAR
echo "Bundling resources..."
cd res
jar uf ../SakuraPlayer.jar .
cd ..

# Create .app bundle with jpackage
echo "Creating macOS .app bundle..."
jpackage \
  --type app-image \
  --input "$PWD" \
  --main-jar SakuraPlayer.jar \
  --main-class App \
  --name "Sakura Player" \
  --icon "$PWD/res/icon.icns" \
  --app-version 1.0 \
  --vendor "Sakura Player" \
  --copyright "Copyright © 2026" \
  --mac-package-name "Sakura Player" \
  --mac-package-identifier com.sakuraplayer.app \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --java-options "-Duser.dir=\${APPDIR}" \
  --module-path "lib" \
  --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web \
  --dest "$PWD/dist"

# Cleanup
rm SakuraPlayer.jar

echo ""
echo "✅ Build complete!"
echo "Application created at: dist/Sakura Player.app"
echo ""
echo "You can now drag this to your Applications folder."
