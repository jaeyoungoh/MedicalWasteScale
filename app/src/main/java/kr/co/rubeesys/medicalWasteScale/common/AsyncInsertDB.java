package kr.co.rubeesys.medicalWasteScale.common;

import android.os.AsyncTask;

public class AsyncInsertDB extends AsyncTask<WeightInfo, Void, Void> {

    private DaoWeightInfo mDaoWeightInfo;

    public AsyncInsertDB(DaoWeightInfo mDaoWeightInfo) {
        this.mDaoWeightInfo = mDaoWeightInfo;
    }

    @Override
    protected Void doInBackground(WeightInfo... daoWeightInfo) {
        mDaoWeightInfo.addWeightValue(daoWeightInfo[0]);
        return null;
    }
}
