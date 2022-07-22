package kr.co.rubeesys.medicalWasteScale;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Set;

import kr.co.rubeesys.medicalWasteScale.common.AppDatabase;
import kr.co.rubeesys.medicalWasteScale.common.AsyncExportDB;
import kr.co.rubeesys.medicalWasteScale.common.AsyncSelectDB;
import kr.co.rubeesys.medicalWasteScale.common.WeightInfo;
import kr.co.rubeesys.medicalWasteScale.databinding.MainBinding;
import kr.co.rubeesys.medicalWasteScale.databinding.MainLeftContentsBinding;

public class MainActivity extends AppCompatActivity {

    //Local DB
    public static AppDatabase localDB;

    // main binding
    public MainBinding mainActivityBinding;
    private MainLeftContentsBinding leftContentsBinding;

    // USB Service
    private UsbService usbService;
    private TextView nowWeightNumber;
    private UsbServiceHandler mUsbServiceHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mUsbServiceHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
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

        mUsbServiceHandler = new UsbServiceHandler(this);
        mainActivityBinding.imageLogo.setOnClickListener(v -> sendingSerialTestData(v));

        //USB 접근 권한 설정
        if (!(Environment.isExternalStorageManager())) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 1225);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 1225);
            }
        }

        //DB 생성
        localDB = AppDatabase.getInstance(this.getApplicationContext());

        //DB 변경 발생시
        localDB.DaoWeightInfo().getAll().observe(this, getData -> {
            Log.i("Medical Waste Scale Test", getData.toString());
            if(getData.isEmpty() || getData.size() <= 0)
                return;
            long selectDateTime = System.currentTimeMillis();
            long firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            new AsyncSelectDB(localDB.DaoWeightInfo(), new AsyncSelectDB.AsyncTaskCallback(){
                @Override
                public void onSuccess(String result) {
                    mainActivityBinding.rightContents.totalWeightNumber.setText(result);
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getApplicationContext(), "Error : DB 입력 오류", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }).execute(Long.valueOf(firstDayOfMonth),Long.valueOf(selectDateTime));

        });
        // csv 파일 저장
        mainActivityBinding.btnSaveSvc.setOnClickListener(v -> saveCsvFile());
    }
    private void sendingSerialTestData(View v){
        final String data = "             5.33 kg            ";
        if(usbService != null)
            usbService.write(data.getBytes());

    }

    private long convertingToMidNightTime(long dtm){
        if(dtm == 0) return 0;
            long convertedDate = 0;
            Date mDtm = new Date(dtm);
            LocalDate convertingDate = mDtm.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDateTime daytoMidnight = LocalDateTime.of(convertingDate, LocalTime.MIDNIGHT);
            convertedDate = daytoMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return convertedDate;
    }

    private String convertingDateToString(long dtm){
        Date getDateTime = new Date(dtm);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(getDateTime);
    }

    private static final int CREATE_FILE = 1;
    private void startActionCreateDocumentForExportIntent(String createdFileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, createdFileName);
        resultLauncher.launch(intent);
    }

    private StringBuilder sbWeightInfo;
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData  != null) {
                        Uri uri = resultData.getData();

                        try(OutputStream outputStream =
                                    getContentResolver().openOutputStream(uri)) {
                            if(outputStream != null){
                                outputStream.write(sbWeightInfo.toString().getBytes());
                            }

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });

    private List<WeightInfo> mWeightInfoList;
    private void saveCsvFile(){
        long selectDateTime = System.currentTimeMillis();
        long firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        new AsyncExportDB(localDB.DaoWeightInfo(), new AsyncExportDB.AsyncTaskCallback() {
            @Override
            public void onSuccess(List<WeightInfo> weightInfoList) {
                mWeightInfoList = weightInfoList;

                try {
                    sbWeightInfo = new StringBuilder();
                    sbWeightInfo.append("No");
                    sbWeightInfo.append(",");
                    sbWeightInfo.append("Date Time");
                    sbWeightInfo.append(",");
                    sbWeightInfo.append("Weight Value");
                    sbWeightInfo.append("\n");

                    if(mWeightInfoList != null) {
                        for(WeightInfo weightInfo : mWeightInfoList)
                        {
                            sbWeightInfo.append(weightInfo.getUid());
                            sbWeightInfo.append(",");
                            sbWeightInfo.append(convertingDateToString(weightInfo.getCreateDateTime()));
                            sbWeightInfo.append(",");
                            sbWeightInfo.append(weightInfo.getWeightValue());
                            sbWeightInfo.append("\n");
                        }
                    }
                    startActionCreateDocumentForExportIntent(nowDate() + "_OutputFile.csv");
                }   catch (Exception e) {
                    Log.e("IOException", "exception in saveCsvFile() method");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Error : Data를 저장하는 도중 에러가 발생하였습니다.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }).execute(Long.valueOf(firstDayOfMonth),Long.valueOf(selectDateTime));
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

}