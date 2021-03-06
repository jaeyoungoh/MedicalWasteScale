package kr.co.rubeesys.medicalWasteScale;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import kr.co.rubeesys.medicalWasteScale.common.AsyncInsertDB;
import kr.co.rubeesys.medicalWasteScale.common.WeightInfo;

public class UsbServiceHandler extends Handler{

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
        private static WeakReference<MainActivity> mActivity;

        public UsbServiceHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:

                    String data = convertWeightValue(msg);
                    mActivity.get().mainActivityBinding.leftContents.nowWeightNumber.setText(data); //시리얼 데이터 연동 Display

                    //Db Insert
                    WeightInfo weightInfo = new WeightInfo();
                    weightInfo.setWeightValue(Double.parseDouble(data));
                    weightInfo.setCreateDateTime(System.currentTimeMillis());

                    new AsyncInsertDB(MainActivity.localDB.DaoWeightInfo()).execute(weightInfo);
                    //Log.d("Inter to DB",MainActivity.localDB.DaoWeightInfo().getAll().toString());

                    //시리얼 데이터 초기화
                    this.removeMessages(UsbService.INIT_VALUE);
                    this.sendEmptyMessageDelayed(UsbService.INIT_VALUE, 5000);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS 변화 감지됨",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR 변화 감지됨",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.INIT_VALUE:
                    mActivity.get().mainActivityBinding.leftContents.nowWeightNumber.setText("0.00"); //시리얼 데이터 초기화
                    break;
            }
        }

        private String convertWeightValue (Message msg)
        {
            float result = 0.0f;
            final String data[] = ((String) msg.obj).trim().split(" ");
            if(data == null || data[0].isEmpty())
                return "0.00";
            else
            {
                result = Float.parseFloat(data[0]);
            }
            return String.format("%.2f",result);
        }
}
