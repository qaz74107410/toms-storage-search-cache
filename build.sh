#!/bin/sh
# Builds build/TomsStorageSearchCache-1.20.1-<version>.jar without Gradle, compiling against
# the libraries a Forge 1.20.1-47.4.13 install already has.
#   LIBS  libraries folder of the launcher (default: PrismLauncher on Windows)
#   MODS  mods folder containing toms_storage-1.20-1.7.1.jar and emi-1.1.24+1.20.1+forge.jar
#   JAVA_HOME  JDK 17 or newer
set -e
cd "$(dirname "$0")"
VERSION=1.1.0
FORGE=1.20.1-47.4.13
LIBS="${LIBS:-$APPDATA/PrismLauncher/libraries}"
: "${MODS:?set MODS to a mods folder with Tom's Simple Storage and EMI}"
JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
JAR="${JAVA_HOME:+$JAVA_HOME/bin/}jar"

case "$(uname -s)" in
	MINGW*|MSYS*|CYGWIN*) SEP=';'; LIBS=$(cygpath -m "$LIBS"); MODS=$(cygpath -m "$MODS") ;;
	*) SEP=':' ;;
esac
CP="$LIBS/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar"
CP="$CP$SEP$LIBS/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar"
CP="$CP$SEP$LIBS/com/electronwill/night-config/core/3.6.4/core-3.6.4.jar"
CP="$CP$SEP$LIBS/net/minecraftforge/fmlcore/$FORGE/fmlcore-$FORGE.jar"
CP="$CP$SEP$LIBS/net/minecraftforge/javafmllanguage/$FORGE/javafmllanguage-$FORGE.jar"
CP="$CP$SEP$LIBS/net/minecraftforge/forge/$FORGE/forge-$FORGE-universal.jar"
CP="$CP$SEP$LIBS/net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-srg.jar"
CP="$CP$SEP$MODS/toms_storage-1.20-1.7.1.jar"
CP="$CP$SEP$MODS/emi-1.1.24+1.20.1+forge.jar"

rm -rf build
mkdir -p build/classes
"$JAVAC" --release 17 -proc:none -nowarn -cp "$CP" -d build/classes $(find src/main/java -name '*.java')
mkdir -p build/res
cp -r src/main/resources/. build/res/
rm build/res/META-INF/MANIFEST.MF
"$JAR" --create --file "build/TomsStorageSearchCache-1.20.1-$VERSION.jar" \
	--manifest src/main/resources/META-INF/MANIFEST.MF -C build/classes . -C build/res .
echo "built build/TomsStorageSearchCache-1.20.1-$VERSION.jar"
