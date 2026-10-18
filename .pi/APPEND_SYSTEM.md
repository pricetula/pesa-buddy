# PesaBuddy Project System Instructions

When generating or editing code in this directory (`~/AndroidStudioProjects/PesaBuddy`), apply the PesaBuddy architecture rules automatically:

- **Stack:** Kotlin + Jetpack Compose (Material 3) + Room + SQLCipher + WorkManager + BroadcastReceiver (SMS).
- **Offline-first:** Room is source of truth; Supabase sync only for paid tier with RLS (`owner` / `shift_cashier`).
- **SMS Parsing:** Use deterministic regex; include `UnparsedInboxItem` fallback; compile regex once; support user-added custom headers.
- **Compliance:** Google Play SMS disclosure is mandatory (`ProminentDisclosureScreen`); never request `READ_SMS` / `RECEIVE_SMS` without it.
- **Security:** SQLCipher AES-256 with Android Keystore (`KeyStore`); never store DB keys in `SharedPreferences` or `local.properties`.
- **Background:** Use `WorkManager` (`OneTimeWorkRequest` / `PeriodicWorkRequest`) for sync/reconciliation; `BroadcastReceiver` for SMS interception only.
- **Build:** Kotlin DSL (`build.gradle.kts`); Version Catalogs (`gradle/libs.versions.toml`); Compose BOM.
- **Tests:** Maintain `test/` for parser/unit logic, `androidTest/` for Compose / SMS pipeline; target >70% coverage for SMS, sync, and security code.

Refer to `.pi/skills/kotlin-android/SKILL.md` and root `AGENT.md` for full details.

IMPORTANT — If working on Kotlin / Android / Compose / Room / SMS / Sync code in this repo: always invoke `/skill:kotlin-android-architecture` at session start, or ensure the skill loads automatically via its routing description.
