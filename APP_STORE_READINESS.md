# CycleSync — App Store Readiness Review

**Project:** CycleSync (Android, Kotlin + Jetpack Compose)
**Architecture:** Clean Architecture (MVVM), Room + SQLCipher, Hilt DI
**Version:** 1.0.0 (versionCode 1) | Min SDK 26 | Target SDK 34

---

## What's Strong (Ready for Store)

| Area | Status | Notes |
|------|--------|-------|
| **Core Features** | Done | Period tracking, fertility prediction, symptom logging, analysis, CSV export, onboarding |
| **Security** | Excellent | SQLCipher AES-256 encryption, no internet permission, FLAG_SECURE, Android Keystore keys |
| **Privacy** | Excellent | 100% offline, zero telemetry, no third-party SDKs, backup disabled |
| **Data Model** | Done | 13 well-designed Room entities with foreign keys, indices, referential integrity |
| **Prediction Engine** | Done | Bayesian priors (Bull 2019, 612K cycles), age-stratified, fertile window calc (Wilcox 2000) |
| **Notifications** | Done | 5 types with 3 privacy levels, AlarmManager, boot persistence, proper channel setup |
| **Offline Support** | Done | Entirely offline by design — works with no network, no Google Play services |
| **Tests** | Good | 22 test files covering domain, data, UI layers (~4,500 lines) |
| **Code Quality** | Excellent | Zero TODO/FIXME/HACK comments, clean architecture, proper Kotlin idioms |
| **ProGuard** | Configured | Minification + resource shrinking enabled for release builds |

---

## What's Blocking Store Submission (Must Fix)

### 1. No Release Signing Configuration
There is no keystore or signing config in `app/build.gradle.kts`. Google Play requires a signed AAB.

**Needed:**
- Generate a release keystore (`.jks`)
- Add `signingConfigs { release { ... } }` block in build.gradle.kts
- Wire it to the release build type
- Store credentials securely (not in version control)

### 2. No Privacy Policy
Google Play **requires** a privacy policy URL for health/fitness apps. The app has a medical disclaimer in Settings but no actual privacy policy.

**Needed:**
- Write a privacy policy covering: what data is collected (cycle data, symptoms), that it stays on-device, no third-party sharing, data deletion rights
- Host it at a public URL
- Link it in the app (Settings screen) and in the Play Console listing
- This is especially critical since CycleSync handles **sensitive health data** — Play's Health policy applies

### 3. No Store Listing Assets
Nothing exists for the Play Store listing.

**Needed:**
- App icon: 512x512 PNG for the store listing
- Feature graphic: 1024x500
- Screenshots: at minimum 2 phone screenshots, ideally 4-8 showing key flows
- Short description (80 chars) and full description (4000 chars)
- App category: "Health & Fitness"
- Content rating questionnaire (IARC)

### 4. Incomplete App Lock Feature
The Settings screen shows an "App lock" row with "Not set," and biometric dependencies are imported, but there is no PIN entry screen, no biometric prompt, and no app lock enforcement on launch. This is a broken UX.

**Fix:** Either complete the implementation (PIN entry + biometric prompt on launch) or remove the UI element entirely before shipping.

### 5. No Internationalization (i18n)
All strings are hardcoded in English throughout composables, ViewModels, and the notification system. `strings.xml` only contains `app_name`.

**Needed:**
- Extract all user-facing strings to `strings.xml`
- At minimum, prepare for localization even if only English ships initially

### 6. Data Safety Form
Google Play requires a Data Safety section declaring:
- No data shared with third parties
- No data collected or transmitted off-device
- Data encrypted at rest
- User can request deletion (already supported)

---

## What Should Be Fixed Before Launch (High Priority)

### 7. No Crash Reporting or Logging
Exceptions are caught with generic `catch (_: Exception)` blocks and silently discarded. No Timber, no Crashlytics, no Sentry. Production bugs will be invisible.

### 8. Accessibility Gaps
- The CycleRing visualization has no screen reader description
- Calendar uses color-only differentiation (problematic for colorblind users)
- No font size / high contrast settings
- No TalkBack optimization for tracking entry selection

### 9. Destructive Database Migration
Room uses `fallbackToDestructiveMigration()` — any schema change in a future update wipes all user data. Implement proper migration strategies before post-launch schema changes.

### 10. Generic Error Messages
Users see "Failed to export data" with no actionable information. Add structured error types and meaningful messages.

---

## Recommended Before Launch (Medium Priority)

| Item | Details |
|------|---------|
| **CI/CD Pipeline** | No GitHub Actions or any CI exists. Set up build + test automation. |
| **Version Strategy** | No changelog, no pre-release tagging. Plan version bumping. |
| **Content Rating** | IARC questionnaire needed for health/medical content. |
| **Backup/Transfer** | Users cannot move data to a new phone. Consider encrypted export/import. |
| **Hardcoded Fallback Key** | `AppModule.kt` has fallback passphrase `"cyclesync-secure-key-v2"`. Evaluate risk. |

---

## Prioritized Action Plan

| Priority | Task | Effort |
|----------|------|--------|
| **P0** | Generate release keystore + signing config | Small |
| **P0** | Write and host a privacy policy | Small |
| **P0** | Complete or remove the app lock feature | Medium |
| **P0** | Fill out Play Store listing (descriptions, screenshots, assets) | Medium |
| **P0** | Complete Data Safety form | Small |
| **P1** | Extract hardcoded strings to `strings.xml` | Medium |
| **P1** | Add crash logging (at least local/debug) | Small |
| **P1** | Fix accessibility: CycleRing descriptions, colorblind support | Medium |
| **P1** | Plan database migration strategy (replace destructive migration) | Medium |
| **P1** | Set up CI/CD (build + test on PRs) | Medium |
| **P2** | Add structured error handling with typed exceptions | Medium |
| **P2** | Encrypted backup/restore for device transfer | Large |
| **P2** | Localization for top languages | Large |

---

## Bottom Line

The **core app is solid** — clean architecture, strong security, good test coverage, complete feature set, and a genuine privacy-first approach. The codebase is at roughly **85% readiness**.

The blockers are all **around the app**, not in it: signing configuration, privacy policy, store listing assets, and the incomplete app lock feature. The P0 items are what stand between the current state and a first Play Store submission.
