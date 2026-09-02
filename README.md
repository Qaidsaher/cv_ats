# ResumeCraft - ATS-Friendly Resume Builder for Android

[![Build & Release Android APK](https://github.com/OWNER/REPOSITORY/actions/workflows/android-release.yml/badge.svg)](../../actions/workflows/android-release.yml)
[![Latest Release](https://img.shields.io/github/v/release/OWNER/REPOSITORY?include_prereleases&color=0D9488&label=Latest%20Release)](../../releases/latest)
[![Android Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

A modern, offline-first Android application crafted with Jetpack Compose and Material Design 3 for creating professional, certified ATS-friendly resumes and exporting vector PDFs.

---

## 📱 Download Latest APK Release

Every time you **push to GitHub** (`main` or `master` branch) or create a version tag, **GitHub Actions automatically builds the latest APK and publishes it to GitHub Releases**.

### ⬇️ Direct Download Links

| File | Description | Download Link |
| :--- | :--- | :--- |
| 🚀 **ResumeCraft APK** | **Latest Production Release** *(Recommended)* | [**📥 Download Latest APK**](../../releases/latest/download/ResumeCraft.apk) |
| 📦 **All Releases** | View all previous builds, changelogs, and APKs | [**Browse All Releases**](../../releases) |
| 🛠️ **Workflow Artifacts** | Download raw build artifacts from CI runs | [**View GitHub Actions**](../../actions/workflows/android-release.yml) |

> 💡 **Permanent Direct Download URL Format**:  
> `https://github.com/<YOUR_USERNAME>/<YOUR_REPO>/releases/latest/download/ResumeCraft.apk`

---

## 📲 How to Install the APK on Your Android Device

1. **Download the APK**: Click the [**Download Latest APK**](../../releases/latest/download/ResumeCraft.apk) link above directly from your Android phone's browser, or download it from the [**Releases Tab**](../../releases).
2. **Open the File**: When the download finishes, tap the notification or open the `.apk` file from your **Downloads** folder.
3. **Allow Installation**: If Android prompts you with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Toggle **Allow from this source** to ON.
4. **Install & Launch**: Tap **Install**, then tap **Open** to start building your resume!

---

## ⚙️ Automated CI/CD Workflow (How It Works)

The repository includes a fully automated GitHub Actions workflow in [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml):

```
                       ┌──────────────────────┐
                       │      git push        │
                       │ (main, master, tags) │
                       └──────────┬───────────┘
                                  │
                                  ▼
                       ┌──────────────────────┐
                       │ GitHub Actions CI/CD │
                       │ • Sets up JDK 17     │
                       │ • Builds Release APK │
                       │ • Calculates Version │
                       │ • Signs Application  │
                       └──────────┬───────────┘
                                  │
                                  ▼
                       ┌──────────────────────┐
                       │    GitHub Release    │
                       │ • ResumeCraft.apk    │
                       │ • Release Notes      │
                       │ • SHA256 Checksums   │
                       └──────────────────────┘
```

### 1. Automatic Build on Every Push
Whenever you push commits to `main` or `master`:
- GitHub Actions automatically generates a version identifier `1.0.<build_number>` (e.g. `v1.0.12`).
- Assembles and signs the Android APK.
- Publishes a new GitHub Release with direct download links (`ResumeCraft.apk`, `ResumeCraft-latest.apk`, and `ResumeCraft-v1.0.X.apk`).

### 2. Tagged Semantic Releases
You can also trigger a release with a custom version tag (e.g. `v1.2.0`):
```bash
git tag v1.2.0
git push origin v1.2.0
```
GitHub Actions will automatically build and publish **Release v1.2.0**.

### 3. Manual Release Trigger
1. Go to the **Actions** tab in your GitHub repository.
2. Select **Build & Release Android APK** from the sidebar.
3. Click **Run workflow**, specify an optional version number (e.g., `1.1.0`), and click **Run workflow**.

---

## 🛠️ Local Build Instructions

If you prefer building the APK locally on your machine:

```bash
# Clone the repository
git clone <repository-url>
cd <repository-directory>

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

The compiled APK will be available in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`
