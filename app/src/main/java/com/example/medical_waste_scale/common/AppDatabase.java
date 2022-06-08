package com.example.medical_waste_scale.common;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = {WeightInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DaoWeightInfo DaoWeightInfo();
}
