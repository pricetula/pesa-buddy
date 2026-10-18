# PesaBuddy — AI Coding Context

Project: `PesaBuddy` Android app (`com.example.pesabuddy`) — offline-first finance tracker.
Location: `~/AndroidStudioProjects/PesaBuddy` (repo: https://github.com/pricetula/pesa-buddy)
Tech: Kotlin 2.1+, Jetpack Compose (Material 3), Room 2.6+, SQLCipher 4.5+, WorkManager 2.9+, Supabase Kotlin 2.x.

## Architecture Overview
- **Offline-first:** Room + SQLCipher (AES-256 via Android Keystore) is the source of truth.
- **SMS Ingestion:** BroadcastReceiver intercepts MPESA / bank SMS; deterministic regex parsers; >98% accuracy; Unparsed Inbox fallback; user-defined custom headers.
- **Background:** WorkManager for sync/reconciliation; BroadcastReceiver for SMS.
- **Paid Tier:** Supabase PostgreSQL with Row-Level Security (`owner` vs `shift_cashier`); End-of-Day cash drawer reconciliation.

## Critical Compliance & Security Rules (Always Apply)
1. **Google Play SMS Permissions:** Must include Prominent Disclosure UI before `READ_SMS` / `RECEIVE_SMS`; document core functionality in manifest / Play Console; never request silently.
2. **Encryption:** All local DB via SQLCipher; key from Android Keystore (`KeyStore.getInstance("AndroidKeyStore")`); never in `SharedPreferences`, `local.properties`, or source.
3. **Dual Mode:** Free mode = zero network; Paid mode = Supabase sync only when feature flag `isPaidTier == true`; server-side RLS must match client checks.
4. **Regex Parsing:** Compile patterns once (`Regex.compile()`); support custom bank/wallet headers; always include `UnparsedInboxItem` fallback.
5. **Build:** Kotlin DSL (`build.gradle.kts`); use `gradle/libs.versions.toml`; Compose BOM managed centrally.

## AI Coding Preferences for This Project
- Prefer Compose over XML for new screens.
- Keep DB operations in Repository / ViewModel `viewModelScope`; never in Compose `@Composable` direct calls.
- Use `StateFlow` / `MutableStateFlow` for UI state.
- Add SMS parser tests (`test/`) for every new regex pattern.
- When generating SQLCipher/Room code, always include key derivation from Keystore.

## Skill Reference
Load skill `kotlin-android-architecture` (`/skill:kotlin-android-architecture`) for detailed architecture rules, module structure, testing targets, and dependency rules.

When working on this repo, always check `gradle/libs.versions.toml` and `build.gradle.kts` before proposing dependency changes.
