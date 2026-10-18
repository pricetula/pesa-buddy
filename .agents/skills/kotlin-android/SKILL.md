---
name: kotlin-android-architecture
description: Kotlin Android Jetpack Compose architecture for PesaBuddy — offline-first mobile with Room/SQLCipher, SMS ingestion, WorkManager, and Supabase sync. Use when generating Kotlin, Compose, Room, SQLCipher, WorkManager, BroadcastReceiver, or Supabase code.
license: MIT
compatibility: Android SDK 24+, Kotlin 2.1+, Jetpack Compose BOM, Room 2.6+, SQLCipher 4.5+, WorkManager 2.9+, Supabase Kotlin 2.x
---

# Kotlin Android Architecture — PesaBuddy

## Project Identity
- **App:** PesaBuddy (offline-first personal / business finance tracker)
- **Package:** `com.example.pesabuddy`
- **Module:** `app` (single-module for now; plan `core`, `sms-parser`, `sync` modules for scale)
- **Compile SDK:** 37 | **Min SDK:** 24 | **Target SDK:** 37 | **Java:** VERSION_11
- **Build:** Kotlin DSL (`build.gradle.kts`), Version Catalogs (`gradle/libs.versions.toml`)

## Mandatory Stack Rules
- **UI:** Jetpack Compose only; no XML layouts for new screens.
- **Compose:** Material 3 (`androidx.compose.material3`), compose BOM via `libs.androidx.compose.bom`.
- **Local DB:** Room (`androidx.room`) with SQLCipher (`net.zetetic:android-database-sqlcipher` or `net.zetetic:android-database-sqlcipher:4.5.4`) and `SQLiteDatabase.HOOK` for AES-256.
- **SQLite Encryption:** `SQLCipher` via `net.zetetic.android.database.sqlcipher.SQLiteDatabase` with `PRAGMA key = '...'` via Android Keystore (`KeyStore.getInstance("AndroidKeyStore")`). Do NOT store keys in `SharedPreferences` or `local.properties`.
- **Background:** `WorkManager` (`androidx.work`) for sync/reconciliation; `BroadcastReceiver` for SMS interception (`android.provider.Telephony.SMS_RECEIVED`).
- **Sync:** Supabase Kotlin (`io.github.jan.supabase:supabase-compose-kt` / `supabase-kt`) on paid tier only; never call Supabase from free-tier code paths.

## Core Module Rules

### 1. SMS Ingestion & Parsing (Module: `sms-ingestion` or `app/src/main/java/.../sms/`)
- **Trigger:** `BroadcastReceiver` registered for `android.provider.Telephony.SMS_RECEIVED`; declare in `AndroidManifest.xml` with `android:permission="android.permission.BROADCAST_SMS"` (if needed) and `RECEIVE_SMS`.
- **Parsing:** Deterministic regex patterns, not ML/NLP, to hit >98% accuracy target.
- **Supported headers:** `MPESA`, bank shortcodes (`KCB`, `EQUITY`, `COOP`), wallet headers (`M-PESA`, `AIRTEL-MONEY`), and **user-added custom headers** via Settings.
- **Fallback:** Unparsed Inbox queue (`UnparsedInboxEntity`) with manual review UI.
- **Regex must be compiled once:** Use `Regex.compile()` stored in a singleton / `object SmsRegexPatterns`; avoid recompilation per SMS.
- **Data class:** `ParsedSms` with fields `amount: Double`, `currency: String`, `sender: String`, `timestamp: Long`, `rawBody: String`, `isCustomHeader: Boolean`.

### 2. Google Play Compliance (Critical for release)
- **Permissions:** `READ_SMS`, `RECEIVE_SMS`, `SEND_SMS` (if needed) are high-risk.
- **Prominent Disclosure:** Before any SMS permission request, show a dedicated `ProminentDisclosureScreen` (Compose screen) explaining why SMS is needed (automatic expense tracking) and that no personal messages are read. Must be user-initiated (not auto-triggered).
- **Runtime flow:** `PermissionState` from Accompanist (or `rememberLauncherForActivityResult`) → `ProminentDisclosureUI` → `requestPermissions()`.
- **New policy (2024+):** Google Play restricts SMS permissions to apps whose core functionality requires SMS. Document that PesaBuddy is a financial-management app with SMS-based expense parsing.
- **Never:** Request SMS permissions silently or bundle with unrelated permissions.

### 3. Dual-Mode Logic (Free vs Paid)
- **Free Personal Mode:** `LocalRepo` only; Room DB; zero network calls; offline-first.
- **Paid Business Mode:** Unlocks `SupabaseSyncRepo`; RLS policies enforced server-side (`supabase/postgrest` with `.eq("user_id", currentUser)` and `.eq("shift_id", activeShift)`); End-of-Day reconciliation (`ReconciliationWorker` using `WorkManager`).
- **Feature flags:** Use `FeatureFlag` enum / `DataStore` preferences (`isPaidTier: Boolean`, `shiftRole: Owner | Cashier`). Never expose Supabase URLs/anon keys in free mode.
- **RLS Roles:** `owner` (full access + reconciliation) vs `shift_cashier` (read/write only to assigned shift + cash drawer). Server-side RLS must mirror client-side checks.
- **Reconciliation:** `ReconciliationWorker` runs at end-of-day (`WORK_TYPE_PERIODIC`, `PeriodicWorkRequest`); calculates cash drawer totals vs SMS-parsed transactions; writes results to `ReconciliationResult` table.

## Database Design (Room + SQLCipher)
- **Entities:** `Transaction`, `SmsLog`, `UnparsedInboxItem`, `CustomHeader`, `SyncStatus`, `ReconciliationResult`, `Shift`, `CashDrawer`
- **Encryption:** Room `RoomDatabase` subclass passes `SupportSQLiteDatabase` with SQLCipher factory; key from `AndroidKeyStore` via `KeyGenerator` / `KeyStore.getInstance("AndroidKeyStore")`.
- **Migration strategy:** `Migration(1, 2)` etc. Increment `version` in `@Database`; always provide `fallbackToDestructiveMigration()` only in debug builds via `Room.inMemoryDatabaseBuilder` or conditional `.fallbackToDestructiveMigration()`.

## Compose / UI Architecture
- **Theme:** `MaterialTheme` with custom `Color.kt`, `Type.kt`, `Theme.kt` in `ui/theme/`.
- **Screens:** Use `Scaffold` + `TopAppBar`; `NavHost` for navigation (if multi-screen); `LazyColumn` for transaction lists.
- **State:** `ViewModel` + `StateFlow`/`MutableStateFlow`; never hold DB references in Compose state directly. Use `viewModelScope` for coroutines.
- **Offline-first UI:** Show cached data immediately; show `SyncStatusIndicator` (circular progress / last sync timestamp) when online.

## Background Processing Rules
- **WorkManager:** `OneTimeWorkRequest` and `PeriodicWorkRequest`; use `Constraints` (`setRequiresBatteryNotLow(true)` for sync); `BackoffPolicy.EXPONENTIAL`.
- **BroadcastReceiver:** Must be declared in manifest; for SMS, must handle `onReceive()` quickly (<10s) — delegate parsing to `WorkManager` or coroutine via `goAsync()` only if essential.
- **SMS interception:** Do NOT read message content for non-financial SMS. Only parse messages matching known regex patterns; discard others immediately.

## Security & Key Management
- **Keystore:** `KeyStore.getInstance("AndroidKeyStore")`; generate key with `KeyGenParameterSpec` (`KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT`); `setBlockModes(KeyProperties.BLOCK_MODE_GCM)`; `setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)`.
- **SQLCipher key:** Derive from Keystore key or use `KeyStore`-derived AES-256 key; do NOT pass raw key strings in `RoomDatabase.Callback`.
- **Network:** All Supabase calls over HTTPS; no `cleartextTrafficPermitted` in release; pin Supabase endpoint if possible.

## Testing Rules
- **Unit:** `test/` — test regex parsers (`SmsParserTest`), ViewModel logic (`TransactionViewModelTest`), Room DAO (`TransactionDaoTest` with `Room.inMemoryDatabaseBuilder` using SQLCipher in-memory).
- **Instrumented:** `androidTest/` — SMS parsing pipeline with `ShadowBroadcastReceiver` or real device; compose UI tests with `createComposeRule()`.
- **Coverage target:** >70% for `sms-ingestion`, `sync`, and `security` packages.

## Dependency Rules (Version Catalogs)
- Add new dependencies to `gradle/libs.versions.toml`; never hardcode versions in `build.gradle.kts`.
- Compose BOM managed centrally.
- SQLCipher version pinned; do not upgrade without testing key derivation.
- Supabase SDK version tested with current Kotlin coroutines version (`kotlinx-coroutines-android`).

## Files / Directories for AI Context
- `app/src/main/java/com/example/pesabuddy/MainActivity.kt`
- `app/src/main/java/com/example/pesabuddy/ui/theme/` (Color, Theme, Type)
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `.pi/skills/kotlin-android/SKILL.md` (this file)
- `AGENT.md` / `.pi/APPEND_SYSTEM.md` (project-level AI instructions)
