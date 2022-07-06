package kr.co.rubeesys.medicalWasteScale.common;

import android.os.AsyncTask;
import java.lang.Long;

public class AsyncSelectDB extends AsyncTask<Long, Void, Void> {

    private AsyncTaskCallback mCallBack;
    private Exception mException;

    private DaoWeightInfo mDaoWeightInfo;
    public String totalWeight ;

    public AsyncSelectDB(DaoWeightInfo daoWeightInfo, AsyncTaskCallback callback) {
        this.mDaoWeightInfo = daoWeightInfo;
        this.mCallBack = callback;
    }

    @Override
    protected Void doInBackground(Long... startDateTime) {
        try
        {
            totalWeight = String.valueOf(mDaoWeightInfo.totalWeightValues(startDateTime[0].longValue()));
        }
        catch (Exception e)
        {
            mException = e;
        }
        //test code
//        totalWeight = "10.4";
        return null;
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mCallBack != null && mException == null){
            mCallBack.onSuccess(totalWeight);
        } else {
            mCallBack.onFailure(mException);
        }
    }
        public interface AsyncTaskCallback {
        void onSuccess(String result);
        void onFailure(Exception e);
    }
}
