package com.example.medical_waste_scale;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.medical_waste_scale.common.AppDatabase;
import com.example.medical_waste_scale.common.RoomTypeConverter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView weightNumber;

        //네비게이션 바(소프트키) 숨김
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //화면을 켜진 상태로 유지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //DB 생성
        final AppDatabase localDB = Room.databaseBuilder(this, AppDatabase.class, "WeightInfo")
                .addTypeConverter(new RoomTypeConverter())
                .allowMainThreadQueries()
                .build();

        //Live DATA Observe 연결

        localDB.DaoWeightInfo().getAll().observe(this, getData -> {
            Log.i("Medical Waste Scale Test", getData.toString());
        });

        View receiveBtn = findViewById(R.id.image_logo);
        weightNumber = findViewById(R.id.weight_number);

        receiveBtn.setOnClickListener(v -> {
//                WeightInfo weightInfo = new WeightInfo();
//                weightInfo.setWeightValue(30);
//                localDB.DaoWeightInfo().addWeightValue(weightInfo);
            localDB.DaoWeightInfo().addWeightValue(30);
            weightNumber.setText("30kg");




//            Log.i("Medical Waste Scale Test", localDB.DaoWeightInfo().getAll().toString());
        });
    }
}