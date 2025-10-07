#!/bin/sh
# 🧩 Fix Android Project Structure Automatically

# Ensure app folder exists
mkdir -p app/src/main/java
mkdir -p app/src/main/res

echo "➡️ Checking misplaced files..."

# Move manifest
if [ -f AndroidManifest.xml ]; then
  mv AndroidManifest.xml app/src/main/
  echo "✅ Moved AndroidManifest.xml"
fi

# Move build.gradle to app if it's the app module one
if [ -f build.gradle ]; then
  # Detect if it’s an app module Gradle file
  if grep -q "com.android.application" build.gradle; then
    mv build.gradle app/
    echo "✅ Moved app build.gradle"
  fi
fi

# Move XMLs (res files)
for file in *.xml; do
  if [ -f "$file" ]; then
    mv "$file" app/src/main/res/
    echo "✅ Moved $file to res/"
  fi
done

# Move Kotlin files
for file in *.kt; do
  if [ -f "$file" ]; then
    mv "$file" app/src/main/java/
    echo "✅ Moved $file to java/"
  fi
done

echo "🎉 Structure fixed! Make sure your Settings.gradle includes ':app'"
