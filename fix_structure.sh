#!/bin/bash
# 🧱 Auto-fix Android project structure

echo "📦 Fixing Android project structure..."

# Create folders if missing
mkdir -p app/src/main/java
mkdir -p app/src/main/res

# Move AndroidManifest.xml
if [ -f "AndroidManifest.xml" ]; then
  mv AndroidManifest.xml app/src/main/
  echo "✅ Moved AndroidManifest.xml"
fi

# Move app-level build.gradle
if [ -f "build.gradle" ]; then
  if grep -q "com.android.application" build.gradle; then
    mkdir -p app
    mv build.gradle app/
    echo "✅ Moved app build.gradle"
  fi
fi

# Move Kotlin source files (*.kt)
for file in *.kt; do
  if [ -f "$file" ]; then
    mv "$file" app/src/main/java/
    echo "✅ Moved $file to java/"
  fi
done

# Move XML files (*.xml)
for file in *.xml; do
  if [ -f "$file" ]; then
    mv "$file" app/src/main/res/
    echo "✅ Moved $file to res/"
  fi
done

echo "🎯 Structure fix complete!"
          if [ -f AndroidManifest.xml ]; then
            mv AndroidManifest.xml app/src/main/
            echo "✅ Moved AndroidManifest.xml"
          fi

          if [ -f build.gradle ]; then
            if grep -q "com.android.application" build.gradle; then
              mv build.gradle app/
              echo "✅ Moved app build.gradle"
            fi
          fi

          for file in *.xml; do
            if [ -f "$file" ]; then
              mv "$file" app/src/main/res/
              echo "✅ Moved $file to res/"
            fi
          done

          for file in *.kt; do
            if [ -f "$file" ]; then
              mv "$file" app/src/main/java/
              echo "✅ Moved $file to java/"
            fi
          done

          echo "🎯 Structure fixed successfully!"

      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk          name: app-debug
          path: app/build/outputs/apk/debug/*.apk          path: app/build/outputs/apk/debug/*.apk          path: app/build/outputs/apk/debug/*.apk          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
          if [ -f AndroidManifest.xml ]; then
            mv AndroidManifest.xml app/src/main/
            echo "✅ Moved AndroidManifest.xml"
          fi

          if [ -f build.gradle ]; then
            if grep -q "com.android.application" build.gradle; then
              mv build.gradle app/
              echo "✅ Moved app build.gradle"
            fi
          fi

          for file in *.xml; do
            if [ -f "$file" ]; then
              mv "$file" app/src/main/res/
              echo "✅ Moved $file to res/"
            fi
          done

          for file in *.kt; do
            if [ -f "$file" ]; then
              mv "$file" app/src/main/java/
              echo "✅ Moved $file to java/"
            fi
          done

          echo "🎉 Structure fixed!"

      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
          if [ -f AndroidManifest.xml ]; then
            mv AndroidManifest.xml app/src/main/
            echo "✅ Moved AndroidManifest.xml"
          fi

          if [ -f build.gradle ]; then
            if grep -q "com.android.application" build.gradle; then
              mv build.gradle app/
              echo "✅ Moved app build.gradle"
            fi
          fi

          for file in *.xml; do
            if [ -f "$file" ]; then
              mv "$file" app/src/main/res/
              echo "✅ Moved $file to res/"
            fi
          done

          for file in *.kt; do
            if [ -f "$file" ]; then
              mv "$file" app/src/main/java/
              echo "✅ Moved $file to java/"
            fi
          done

          echo "🎉 Structure fixed!"

      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
