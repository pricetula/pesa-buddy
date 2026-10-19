package com.example.pesabuddy.database

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object DatabaseKeyProvider {
    private const val PREFS_NAME = "db_key_prefs"
    private const val KEY_ALIAS = "db_key"
    private const val MASTER_KEY_ALIAS = "master_key_pesabuddy"

    fun getDatabaseKey(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val encoded = sharedPreferences.getString(KEY_ALIAS, null)
        if (encoded != null) {
            return Base64.decode(encoded, Base64.DEFAULT)
        }

        val key = ByteArray(32).also {
            java.security.SecureRandom().nextBytes(it)
        }
        sharedPreferences.edit()
            .putString(KEY_ALIAS, Base64.encodeToString(key, Base64.DEFAULT))
            .apply()
        return key
    }
}
