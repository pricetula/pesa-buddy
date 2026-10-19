package com.example.pesabuddy.database

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

object DatabaseModule {
    fun provideDatabase(context: Context): AppDatabase {
        SQLiteDatabase.loadLibs(context)
        val passphrase = DatabaseKeyProvider.getDatabaseKey(context)
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "pesabuddy.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }
}
