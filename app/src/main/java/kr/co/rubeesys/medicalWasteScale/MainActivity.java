package kr.co.rubeesys.medicalWasteScale;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import kr.co.rubeesys.medicalWasteScale.databinding.MainBinding;
import kr.co.rubeesys.medicalWasteScale.databinding.MainLeftContentsBinding;

public class MainActivity extends AppCompatActivity {

    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;
    private Button serialTestButton;

    private int deviceId, portNum, baudRate;
    private boolean withIoManager;

    private enum UsbPermission { Unknown, Requested, Granted, Denied }
    private UsbSerialPort usbSerialPort;
//    private ControlLines controlLines;

    private SerialInputOutputManager usbIoManager;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private TextView receiveText;
    private boolean connected = false;

    private Handler mainLooper;
    private BroadcastReceiver broadcastReceiver;

    private MainBinding viewBinding;
    private MainLeftContentsBinding leftContentsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //네비게이션 바(소프트키) 숨김
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //화면을 켜진 상태로 유지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        leftContentsBinding = MainLeftContentsBinding.inflate(getLayoutInflater());

        updateWeightNumber(leftContentsBinding, "3kg");

    }

    private void updateWeightNumber(MainLeftContentsBinding viewBinding, String num)
    {
        viewBinding.nowWeightNumber.setText(num);
    }
}