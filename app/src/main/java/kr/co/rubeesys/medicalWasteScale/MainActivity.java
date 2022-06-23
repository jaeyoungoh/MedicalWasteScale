package kr.co.rubeesys.medicalWasteScale;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.rubeesys.medicalWasteScale.databinding.MainBinding;
import kr.co.rubeesys.medicalWasteScale.databinding.MainLeftContentsBinding;

public class MainActivity extends AppCompatActivity {

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
        updateWeightNumber(leftContentsBinding, "3.2");

    }

    private void updateWeightNumber(MainLeftContentsBinding viewBinding, String num)
    {
        viewBinding.nowWeightNumber.setText(num);
    }
}