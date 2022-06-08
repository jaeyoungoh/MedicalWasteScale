package com.example.medical_waste_scale.common;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoWeightInfo {
    @Query("SELECT * FROM weightInfo")
    List<WeightInfo> getAll();

    @Query("SELECT * FROM weightInfo where createDateTime >= (:startDateTime)")
    List<WeightInfo> findByWeightInfos(String startDateTime);

    @Query("SELECT * FROM weightInfo where createDateTime between (:startDateTime) and (:endDateTime)")
    List<WeightInfo> findByWeightInfos(String startDateTime, String endDateTime);

    @Insert
    void addWeightValue(WeightInfo weightInfo);

    @Query("INSERT INTO weightInfo(medicalWasteType, createDateTime, weightValue) VALUES('none', 'CURRENT_TIMESTAMP', :weightValue) ")
    void addWeightValue(int weightValue);

    @Update
    void editWeightValue(WeightInfo weightInfo) ;

}
