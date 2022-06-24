package kr.co.rubeesys.medicalWasteScale;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import kr.co.rubeesys.medicalWasteScale.databinding.MainBinding;
import kr.co.rubeesys.medicalWasteScale.databinding.MainLeftContentsBinding;

public class MainActivity extends AppCompatActivity {

    private MainBinding mainActivityBinding;
    private MainLeftContentsBinding leftContentsBinding;

    private UsbService usbService;
    private TextView nowWeightNumber;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityBinding = MainBinding.inflate(getLayoutInflater());
        setContentView(mainActivityBinding.getRoot());

        //네비게이션 바(소프트키) 숨김
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //화면을 켜진 상태로 유지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mHandler = new MyHandler(this);
//        mainActivityBinding.imageLogo.setOnClickListener(v -> sendingSerialTestData());

        // csv 파일 저장
        mainActivityBinding.btnSaveSvc.setOnClickListener(v -> saveCsvFile());

    }
    private void sendingSerialTestData(){
        final String data = "             5.33 kg            ";
        if(usbService != null)
            usbService.write(data.getBytes());
    }

    private void saveCsvFile(){
        File appDir = new File(mainActivityBinding.getRoot().getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),   "Medical_Waste_Scale_data");
        appDir.mkdirs();
        try {
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(mainActivityBinding.getRoot().getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/Medical_Waste_Scale_data/" + nowDate() + "_OutputFile.csv");
                FileOutputStream fos = new FileOutputStream(file);
                String text = nowDateTime() + "," + "5.1";
                fos.write(text.getBytes());
                fos.close();
            }
        }   catch (IOException e) {
            Log.e("IOException", "exception in saveCsvFile() method");
        }
    }

    private String nowDate(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private String nowDateTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB 준비됨", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB 권한 획득 실패", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "USB 케이블 연결상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB 연결 끊김", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "지원되지 않는 USB 케이블 입니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = convertWeightValue(msg);
                    mActivity.get().mainActivityBinding.leftContents.nowWeightNumber.setText(data); //시리얼 데이터 연동
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS 변화 감지됨",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR 변화 감지됨",Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private String convertWeightValue (Message msg)
        {
            float result = 0.0f;
            final String data[] = ((String) msg.obj).trim().split(" ");
            if(data == null)
                return "0.00";
            else
            {
                result = Float.parseFloat(data[0]);
            }
            return String.format("%.2f",result);
        }
    }
}