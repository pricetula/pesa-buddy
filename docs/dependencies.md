# Dependency Choices

| Dependency | Why it was chosen |
|------------|-------------------|
| `androidx.room` + `room-compiler` | Offline-first local database; Room generates boilerplate DAO/Entity code |
| `net.zetetic:android-database-sqlcipher` | AES-256 encryption for SQLite (required for financial data / Google Play security) |
| `androidx.security:security-crypto` | Android Keystore integration — stores DB encryption keys securely |
| `androidx.work:work-runtime-ktx` | Background sync and End-of-Day reconciliation; survives process death |
| `io.github.jan.supabase:postgrest-kt` + `compose-auth` | Paid-tier cloud sync with Row-Level Security (owner vs cashier) |
