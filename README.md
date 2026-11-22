# BILLORA — Fresh Native Android Billing Starter

This is a brand-new Kotlin Android project. It does not contain or reuse LEDGIX source code.

## Included in this starter
- Native Android app written in Kotlin (no Flutter).
- Simple bill entry: party, item, quantity and rate.
- Total calculation.
- Save recent bill records locally on the device.
- Share a bill summary through Android's share sheet.
- GitHub Actions workflow to build and upload a debug APK.

## Build with GitHub Actions
1. Open **Actions** and run **BILLORA Android APK**, or push to `main`.
2. When the workflow succeeds, open the run and download the `BILLORA-debug-apk` artifact.

## Local build
Requires JDK 17 and Gradle 8.9:
```bash
gradle :app:assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Important scope note
This is the clean starter foundation, not a finished full accounting package. It currently stores data locally on one device. GST invoices, PDF/print templates, party ledgers, purchase/stock automation, cloud sync, login and production signing are not implemented yet. Verify the APK on a device before release.