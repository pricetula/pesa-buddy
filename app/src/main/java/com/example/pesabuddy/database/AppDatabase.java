package com.example.pesabuddy.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TransactionEntity.class, UnparsedSmsEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
    public abstract UnparsedSmsDao unparsedSmsDao();
}
