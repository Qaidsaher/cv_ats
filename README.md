# ResumeCraft - ATS-Friendly Resume Builder for Android

A modern, offline-first Android application crafted with Jetpack Compose and Material Design 3 for creating professional, certified ATS-friendly resumes and exporting vector PDFs.

---

## 📥 Download & Direct Installation

Every time code is pushed to this repository or a new version tag is created, GitHub Actions automatically builds the latest APK and publishes it to [GitHub Releases](../../releases).

### How to Install on Android:
1. Go to the **[Releases](../../releases)** tab on GitHub.
2. Under the latest release, tap **`ResumeCraft-vX.X.X.apk`** to download it.
3. Once downloaded, open the `.apk` file from your device notifications or Downloads folder.
4. If prompted, enable **"Install from unknown sources"** or **"Allow from this source"** in Android Settings.
5. Tap **Install** and enjoy!

---

## 🚀 Automated Versioning & GitHub Actions Release

The automated workflow (`.github/workflows/android-release.yml`) handles versioning, building, and publishing seamlessly:

### 1. Automatic Version on Every Push to `main` / `master`
Whenever you push commits to `main` or `master`:
- GitHub Actions automatically assigns the version as `1.0.<build_number>` (e.g. `v1.0.42`).
- Compiles signed Release and Debug APKs.
- Publishes a new GitHub Release with the installable `.apk` assets.

### 2. Custom Semantic Version with Git Tags
To release a specific version (e.g. `v1.2.0`):
```bash
git tag v1.2.0
git push origin v1.2.0
```
GitHub Actions will automatically pick up `1.2.0`, build the APKs, and publish **Release v1.2.0**.

### 3. Manual Trigger from GitHub UI
1. Go to **Actions** -> **Build & Release Android APK**.
2. Click **Run workflow**.
3. (Optional) Enter your custom version name (e.g. `2.0.0`) and click **Run workflow**.

---

## 🛠️ Build Locally

To build the APK locally on your machine:

```bash
# Clone the repository
git clone <repo-url>
cd <repo-name>

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk` or `app/build/outputs/apk/release/app-release.apk`
