package kr.co.rubeesys.medicalWasteScale.common;

import android.os.AsyncTask;

import java.util.List;

public class AsyncExportDB extends AsyncTask<Long, Void, Void> {
    private AsyncTaskCallback mCallBack;
    private Exception mException;

    private DaoWeightInfo mDaoWeightInfo;
    public String totalWeight ;

    List<WeightInfo> weightInfoList = null;

    public AsyncExportDB(DaoWeightInfo daoWeightInfo, AsyncTaskCallback callback) {
        this.mDaoWeightInfo = daoWeightInfo;
        this.mCallBack = callback;
    }

    @Override
    protected Void doInBackground(Long... dtm) {

        try
        {
            weightInfoList  =  mDaoWeightInfo.totalWeight(dtm[0].longValue(),dtm[1].longValue());
        }
        catch (Exception e)
        {
            mException = e;
        }
        return null;
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mCallBack != null && mException == null){
            mCallBack.onSuccess(weightInfoList);
        } else {
            mCallBack.onFailure(mException);
        }
    }
    public interface AsyncTaskCallback {
        void onSuccess(List<WeightInfo> weightInfoList);
        void onFailure(Exception e);
    }
}
