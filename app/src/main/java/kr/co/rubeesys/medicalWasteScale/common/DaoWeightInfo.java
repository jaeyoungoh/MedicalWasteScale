package kr.co.rubeesys.medicalWasteScale.common;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoWeightInfo {

    @Query("SELECT * FROM weightInfo")
    LiveData<List<WeightInfo>> getAll();

    @Query("SELECT SUM(weightValue) FROM weightInfo where createDateTime >= (:startDateTime)")
    String totalWeightValues(long startDateTime);

    @Query("SELECT SUM(weightValue) FROM weightInfo where createDateTime between (:startDateTime) and (:endDateTime)")
    String totalWeightValues(long startDateTime, long endDateTime);

    @Insert
    void addWeightValue(WeightInfo weightInfo);

//    @Query("INSERT INTO weightInfo(medicalWasteType, createDateTime, weightValue) VALUES('none', 'CURRENT_TIMESTAMP', :weightValue)")
//    void addWeightValue(int weightValue);

    @Update
    void editWeightValue(WeightInfo weightInfo);

    @Query("DELETE FROM weightInfo where createDateTime between (:startDateTime) and (:endDateTime)")
    void deleteWeightValues(long startDateTime, long endDateTime);

}
