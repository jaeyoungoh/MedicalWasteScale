package kr.co.rubeesys.medicalWasteScale;

import static kr.co.rubeesys.medicalWasteScale.common.InitString.EMPTY_STRING;

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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Set;

import kr.co.rubeesys.medicalWasteScale.common.AppDatabase;
import kr.co.rubeesys.medicalWasteScale.common.AsyncExportDB;
import kr.co.rubeesys.medicalWasteScale.common.AsyncSelectDB;
import kr.co.rubeesys.medicalWasteScale.common.DatePickerFragment;
import kr.co.rubeesys.medicalWasteScale.common.WeightInfo;
import kr.co.rubeesys.medicalWasteScale.databinding.MainBinding;

public class MainActivity extends AppCompatActivity implements DatePickerFragment.OnDateAdjustedListener {

    //Date picker 태그
    public static String datePickerTag = EMPTY_STRING;

    //Local DB
    public static AppDatabase localDB;

    // main binding
    public static MainBinding mainActivityBinding;

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

        initDate();

        //네비게이션 바(소프트키) 숨김
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //화면을 켜진 상태로 유지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //조회-시작일,종료일 editText 키보드 숨김
        mainActivityBinding.rightContentsSelect.startedEditText.setInputType(InputType.TYPE_NULL);
        mainActivityBinding.rightContentsSelect.endedEditText.setInputType(InputType.TYPE_NULL);

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
//            Log.i("Medical Waste Scale Test", getData.toString());
            if (getData.isEmpty() || getData.size() <= 0)
                return;
            // Edit Text를 확인후 조회.
            watchingEditText();
        });
        // csv 파일 저장
        mainActivityBinding.btnSaveSvc.setOnClickListener(v -> saveCsvFile());

        // Datepicker dialog
        mainActivityBinding.rightContentsSelect.startedEditText.setOnClickListener(v -> callDatePickerDialog(v));
        mainActivityBinding.rightContentsSelect.endedEditText.setOnClickListener(v -> callDatePickerDialog(v));

        // editText SetChange
        mainActivityBinding.rightContentsSelect.startedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                watchingEditText();
            }
        });
        mainActivityBinding.rightContentsSelect.endedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                watchingEditText();
            }
        });

        mainActivityBinding.rightContents.selectWeightHeader.setOnClickListener(v -> {
            mainActivityBinding.rightContents.getRoot().setVisibility(View.GONE);
            mainActivityBinding.rightContentsSelect.getRoot().setVisibility(View.VISIBLE);
        });

        mainActivityBinding.rightContentsSelect.selectWeightHeader.setOnClickListener(v -> {
            mainActivityBinding.rightContents.getRoot().setVisibility(View.VISIBLE);
            mainActivityBinding.rightContentsSelect.getRoot().setVisibility(View.GONE);
        });

    }// onCreate Ends

    private void initDate(){
        long selectStartedDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); // 매월 1일
        long selectEndedDate = System.currentTimeMillis(); // 현재 날짜
        Date startedDate = new Date(selectStartedDate);
        Date endedDate = new Date(selectEndedDate);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

        mainActivityBinding.rightContentsSelect.startedEditText.setText(f.format(startedDate));
        mainActivityBinding.rightContentsSelect.endedEditText.setText(f.format(endedDate));

    }

    private void watchingEditText(){
        //시작일, 종료일 날짜 가져와서 long으로 변경
        String sStartedDate = mainActivityBinding.rightContentsSelect.startedEditText.getText().toString();
        String sEndedDate = mainActivityBinding.rightContentsSelect.endedEditText.getText().toString();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

        long startedDate = 0;
        long endedDate = 0;

        try{
            Date s = f.parse(sStartedDate);
            Date e = f.parse(sEndedDate);

            e.setDate(e.getDate() + 1);
            startedDate = s.getTime();
            endedDate = e.getTime();

            //종료일이 시작일보다 빠를 경우 에러 발생
            if(startedDate > endedDate){
                endedDate = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Error : 종료일은 시작일보다 빠른 수 없습니다.", Toast.LENGTH_LONG).show();
            }

        }catch (ParseException e)
        {
            Toast.makeText(getApplicationContext(), "날짜 데이터 형식 오류 발생", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //비동기 DB 조회
        selecteDB(startedDate, endedDate);
    }

    private void selecteDB(long startedDate , long endedDate){

        long selectStartedDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); // 매월 1일
        long selectEndedDate = System.currentTimeMillis(); // 현재 날짜
        if(startedDate != 0 && endedDate != 0)
        {
            selectStartedDate = startedDate;
            selectEndedDate = endedDate;
        }
        try {
            new AsyncSelectDB(localDB.DaoWeightInfo(), new AsyncSelectDB.AsyncTaskCallback() {
                @Override
                public void onSuccess(String result) {
                    mainActivityBinding.rightContents.totalWeightNumber.setText(result);
                    mainActivityBinding.rightContentsSelect.totalWeightNumber.setText(result);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getApplicationContext(), "Error : DB 입력 오류", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }).execute(Long.valueOf(selectStartedDate), Long.valueOf(selectEndedDate));
        }
        catch (Exception e)
        {
            Toast.makeText(this,"Error : DB 조회 중 에러가 발생하였습니다. 관리자에게 문의해주세요", Toast.LENGTH_LONG).show();
        }
    }

    boolean bOnClicked = false;
    private void callDatePickerDialog(View v) {
        String onFocusedViewName = v.getResources().getResourceEntryName(v.getId());

        if(bOnClicked) {
            return;
        }
        bOnClicked = true;

        switch (onFocusedViewName) {
            case "startedEditText": {
                datePickerTag = "StartedDatePick";
                break;
            }
            case "endedEditText": {
                datePickerTag = "EndedDatePick";
                break;
            }
            default:
                return;
        }
        DatePickerFragment mDialogFragmentDatePickerFragment = new DatePickerFragment(this);

        mDialogFragmentDatePickerFragment.show(getSupportFragmentManager(), datePickerTag);
        bOnClicked = false;
    }

    @Override
    public void onDateAdjusted(String tag, String selectedDate) {

        switch (tag) {
            case "StartedDatePick": {
                mainActivityBinding.rightContentsSelect.startedEditText.setText(selectedDate);
                break;
            }
            case "EndedDatePick": {
                mainActivityBinding.rightContentsSelect.endedEditText.setText(selectedDate);
                break;
            }
            default:
                return;
        }


    }

    private void sendingSerialTestData(View v) {
        final String data = "             5.33 kg            ";
        if (usbService != null)
            usbService.write(data.getBytes());

    }

//    private long convertingToMidNightTime(long dtm) {
//        if (dtm == 0) return 0;
//        long convertedDate = 0;
//        Date mDtm = new Date(dtm);
//        LocalDate convertingDate = mDtm.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        LocalDateTime daytoMidnight = LocalDateTime.of(convertingDate, LocalTime.MIDNIGHT);
//        convertedDate = daytoMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        return convertedDate;
//    }
//
    private String convertingDateToString(long dtm) {
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
                    Intent resultData = result.getData();
                    if (resultData != null) {
                        Uri uri = resultData.getData();

                        try (OutputStream outputStream =
                                     getContentResolver().openOutputStream(uri)) {
                            if (outputStream != null) {
                                outputStream.write(sbWeightInfo.toString().getBytes());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private List<WeightInfo> mWeightInfoList;

    private void saveCsvFile() {
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

                    if (mWeightInfoList != null) {
                        for (WeightInfo weightInfo : mWeightInfoList) {
                            sbWeightInfo.append(weightInfo.getUid());
                            sbWeightInfo.append(",");
                            sbWeightInfo.append(convertingDateToString(weightInfo.getCreateDateTime()));
                            sbWeightInfo.append(",");
                            sbWeightInfo.append(weightInfo.getWeightValue());
                            sbWeightInfo.append("\n");
                        }
                    }
                    startActionCreateDocumentForExportIntent(nowDate() + "_OutputFile.csv");
                } catch (Exception e) {
                    Log.e("IOException", "exception in saveCsvFile() method");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Error : Data를 저장하는 도중 에러가 발생하였습니다.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }).execute(Long.valueOf(firstDayOfMonth), Long.valueOf(selectDateTime));
    }

    private String nowDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private String nowDateTime() {
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
        initDate();
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