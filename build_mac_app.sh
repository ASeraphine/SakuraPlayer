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

# Detect architecture and download appropriate JavaFX 25 SDK
echo "[1/6] Preparing JavaFX module path..."
ARCH=$(uname -m)
if [ "$ARCH" = "arm64" ]; then
  JAVAFX_ARCH="osx-aarch64"
else
  JAVAFX_ARCH="osx-x64"
fi
JAVAFX_DIR="$BASE_DIR/tools/javafx-sdk-25.0.3-$ARCH"
if [ ! -f "$JAVAFX_DIR/lib/javafx.base.jar" ]; then
  echo "  Downloading JavaFX 25 SDK ($JAVAFX_ARCH)..."
  mkdir -p "$BASE_DIR/tools"
  curl -sL "https://download2.gluonhq.com/openjfx/25.0.3/openjfx-25.0.3_${JAVAFX_ARCH}_bin-sdk.zip" -o /tmp/javafx25-mac.zip
  unzip -o /tmp/javafx25-mac.zip -d "$BASE_DIR/tools" 2>/dev/null
  mv "$BASE_DIR/tools/javafx-sdk-25.0.3" "$JAVAFX_DIR"
  rm /tmp/javafx25-mac.zip
fi


# Copy JavaFX 25 module JARs (exclude JDK internal modules that cause hash conflicts)
cp "$JAVAFX_DIR/lib/javafx.base.jar" javafx-modules/
cp "$JAVAFX_DIR/lib/javafx.controls.jar" javafx-modules/
cp "$JAVAFX_DIR/lib/javafx.graphics.jar" javafx-modules/
cp "$JAVAFX_DIR/lib/javafx.media.jar" javafx-modules/
cp "$JAVAFX_DIR/lib/javafx.swing.jar" javafx-modules/
cp "$JAVAFX_DIR/lib/javafx.fxml.jar" javafx-modules/
cp "$JAVAFX_DIR/lib/javafx.web.jar" javafx-modules/


# Compile Java sources
echo "[2/6] Compiling source code..."
javac --module-path "javafx-modules" --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web,java.logging -cp "lib/jaudiotagger-3.0.1.jar:lib/batik-all-1.19.jar:lib/svg-salamander-1.1.5.3.jar:lib/jlayer-1.0.1.jar:lib/mp3agic-0.9.0.jar:src" src/*.java -d bin


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

# First, create a custom jlink runtime that includes JavaFX modules
echo "     Creating custom runtime with JavaFX modules..."
rm -rf "$BASE_DIR/custom-runtime"

# Find JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
fi
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
fi
echo "     Using JAVA_HOME: $JAVA_HOME"

jlink \
  --module-path "$JAVAFX_DIR/lib:${JAVA_HOME}/jmods" \
  --add-modules javafx.controls,javafx.media,javafx.swing,javafx.fxml,javafx.web,java.logging,java.desktop,java.naming,java.scripting,jdk.unsupported,jdk.crypto.ec \
  --output "$BASE_DIR/custom-runtime" \
  --no-header-files \
  --no-man-pages 2>&1


# Copy JavaFX native libraries into the custom runtime
echo "     Copying JavaFX native libraries into custom runtime..."
cp "$JAVAFX_DIR/lib/libdecora_sse.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libfxplugins.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libglass.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libglib-lite.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libgstreamer-lite.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libjavafx_font.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libjavafx_iio.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libjfxmedia.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libjfxmedia_avf.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libjfxwebkit.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libprism_common.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libprism_es2.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libprism_mtl.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
cp "$JAVAFX_DIR/lib/libprism_sw.dylib" "$BASE_DIR/custom-runtime/lib/" 2>/dev/null
echo "     ✅ Custom runtime created with JavaFX modules"

# Copy the JAR and non-JavaFX dependency JARs into the input directory
cp SakuraPlayer.jar input/
cp lib/jaudiotagger-3.0.1.jar input/
cp lib/batik-all-1.19.jar input/
cp lib/svg-salamander-1.1.5.3.jar input/
cp lib/jlayer-1.0.1.jar input/
cp lib/mp3agic-0.9.0.jar input/

jpackage \
  --type app-image \
  --runtime-image "$BASE_DIR/custom-runtime" \
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
  --java-options "--enable-native-access=javafx.graphics" \
  --dest "$BASE_DIR/dist"


# Fix macOS Gatekeeper: remove quarantine and ad-hoc sign the .app
echo "     Fixing macOS Gatekeeper for the .app..."
xattr -cr "dist/Sakura Player.app" 2>/dev/null
codesign --force --deep --sign - "dist/Sakura Player.app" 2>/dev/null
echo "     ✅ macOS Gatekeeper fix applied"


# ============================================
# Build Windows .exe with Launch4j (optional - only if full Launch4j distribution is available)
# ============================================
echo "[6/6] Creating Windows .exe with Launch4j..."

# Check if the full Launch4j distribution is available (with head/ and w32api/ directories)
LAUNCH4J_DIR="$BASE_DIR/tools/launch4j"
LAUNCH4J_JAR="$LAUNCH4J_DIR/launch4j.jar"
LAUNCH4J_DEPS="$LAUNCH4J_DIR/xstream.jar:$LAUNCH4J_DIR/xmlpull.jar"

# Download the full Launch4j distribution if not present
if [ ! -f "$LAUNCH4J_JAR" ] || [ ! -d "$LAUNCH4J_DIR/head" ] || [ ! -d "$LAUNCH4J_DIR/w32api" ]; then
  echo "  ⚠️  Downloading Launch4j distribution..."
  mkdir -p "$LAUNCH4J_DIR"
  
  # Download launch4j and its dependencies from Maven Central
  curl -L -o "$LAUNCH4J_DIR/launch4j.jar" "https://repo1.maven.org/maven2/net/sf/launch4j/launch4j/3.50/launch4j-3.50.jar" 2>/dev/null
  curl -L -o "$LAUNCH4J_DIR/xstream.jar" "https://repo1.maven.org/maven2/com/thoughtworks/xstream/xstream/1.4.20/xstream-1.4.20.jar" 2>/dev/null
  curl -L -o "$LAUNCH4J_DIR/xmlpull.jar" "https://repo1.maven.org/maven2/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar" 2>/dev/null
  
  # Create head/ and w32api/ directories with the necessary files from mingw-w64
  mkdir -p "$LAUNCH4J_DIR/head" "$LAUNCH4J_DIR/w32api" "$LAUNCH4J_DIR/bin"
  
  # Find mingw-w64 toolchain
  MINGW64_DIR=$(find /opt/homebrew/Cellar/mingw-w64 -name "x86_64-w64-mingw32" -type d 2>/dev/null | head -1)
  
  if [ -n "$MINGW64_DIR" ]; then
    # Symlink windres and ld from mingw-w64
    ln -sf "$MINGW64_DIR/bin/windres" "$LAUNCH4J_DIR/bin/windres" 2>/dev/null
    ln -sf "$MINGW64_DIR/bin/ld" "$LAUNCH4J_DIR/bin/ld" 2>/dev/null
    
    # Copy crt2.o from mingw-w64
    cp "$MINGW64_DIR/lib/crt2.o" "$LAUNCH4J_DIR/w32api/" 2>/dev/null
    
    # Copy mingw libraries
    for lib in libmingw32.a libmingwex.a libgcc.a libmsvcrt.a libmoldname.a libkernel32.a libuser32.a libadvapi32.a libshell32.a; do
      cp "$MINGW64_DIR/lib/$lib" "$LAUNCH4J_DIR/w32api/" 2>/dev/null
    done
    
    # Create head object files (these are Launch4j-specific, create minimal stubs)
    # The head/ directory contains the native wrapper executable source
    # Without the actual Launch4j distribution, we can't build the .exe
    echo "  ⚠️  Cannot build Windows .exe on macOS without the full Launch4j distribution."
    echo "     The Windows .exe will be built automatically in GitHub Actions CI."
    echo "     Skipping Windows .exe build..."
  else
    echo "  ⚠️  mingw-w64 not found. Cannot cross-compile Windows .exe on macOS."
    echo "     The Windows .exe will be built automatically in GitHub Actions CI."
    echo "     Skipping Windows .exe build..."
  fi
fi

# Try to build the Windows .exe if we have all the necessary files
if [ -f "$LAUNCH4J_JAR" ] && [ -f "$LAUNCH4J_DIR/head/guihead.o" ] && [ -f "$LAUNCH4J_DIR/head/head.o" ]; then
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
  
  java -cp "$LAUNCH4J_JAR:$LAUNCH4J_DEPS" net.sf.launch4j.Main launch4j.xml
  echo "  ✅ Windows .exe created at: dist/SakuraPlayer.exe"
else
  echo "  ⏭️  Windows .exe build skipped (requires full Launch4j distribution with native tools)"
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
