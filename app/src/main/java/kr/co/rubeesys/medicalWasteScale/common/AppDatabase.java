package kr.co.rubeesys.medicalWasteScale.common;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@TypeConverters({DateTimeTypeConverter.class})
@Database(entities = {WeightInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DBName = "WeightInf";
    private static AppDatabase appDatabase;
    public abstract DaoWeightInfo DaoWeightInfo();

    public static synchronized AppDatabase getInstance(Context context)
    {
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class, DBName)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return appDatabase;
    }

}
