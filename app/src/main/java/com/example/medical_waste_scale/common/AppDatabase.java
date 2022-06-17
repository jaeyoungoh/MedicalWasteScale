package com.example.medical_waste_scale.common;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {WeightInfo.class}, version = 1)
@TypeConverters({RoomTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract DaoWeightInfo DaoWeightInfo();
}
