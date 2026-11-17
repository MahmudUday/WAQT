# ওয়াক্ত (WAQT) — Native Android Prayer, Tahajjud & Sehri-Iftar App

Offline-first native Kotlin + Jetpack Compose Android application crafted for Bangladeshi Muslims.

## Building APK on GitHub Actions (No PC Required)

1. Push this project to your GitHub repository (via **AI Studio → Push to GitHub** or by uploading the exported ZIP).
2. Open your repository on GitHub (mobile browser or GitHub Android app) and go to the **Actions** tab.
3. Select **Build WAQT Android APK** (it runs automatically on push to `main`, `master`, or `feature/**`, or tap **Run workflow** manually).
4. Once the workflow finishes, scroll to **Artifacts** and download **`WAQT-Debug-APK`** (`WAQT-v2.0.0-debug.apk`) directly to your phone.

### Optional: Signed Release APK via GitHub Secrets
To also build a signed release APK automatically, add these three Repository Secrets under **Settings → Secrets and variables → Actions**:
- `UPLOAD_KEYSTORE_BASE64`: Base64-encoded `.jks` keystore (`keyAlias` = `upload`)
- `STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Key password
