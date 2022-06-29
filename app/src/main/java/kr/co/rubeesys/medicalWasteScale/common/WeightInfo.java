package kr.co.rubeesys.medicalWasteScale.common;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity
public class WeightInfo {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "createDateTime", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters({DateTimeTypeConverter.class})
    private Date createDateTime;

    @ColumnInfo(name = "medicalWasteType", defaultValue = "none")
    private String medicalWasteType;

    @ColumnInfo(name = "weightValue", defaultValue = "0")
    @NonNull
    private int weightValue;

    public Date getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
    }
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getMedicalWasteType() {
        return medicalWasteType;
    }

    public void setMedicalWasteType(String medicalWasteType) {
        this.medicalWasteType = medicalWasteType;
    }

    public int getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(int weightValue) {
        this.weightValue = weightValue;
    }

    @Override
    public String toString() {
        return "WeightInfo{" +
                "uid=" + uid +
                ", createDateTime='" + createDateTime + '\'' +
                ", medicalWasteType='" + medicalWasteType + '\'' +
                ", weightValue=" + weightValue +
                '}';
    }
}
