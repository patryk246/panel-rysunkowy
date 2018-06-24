package com.example.patryk.rysowanie;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static int kolor=Color.RED;
    public static boolean isCleared=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickRedButton(View view){
        kolor=Color.RED;
    }

    public void onClickYellowButton(View view){
        kolor=Color.YELLOW;
    }

    public void onClickBlueButton(View view){
        kolor=Color.BLUE;
    }

    public void onClickGreenButton(View view){
        kolor=Color.GREEN;
    }

    public void onClickClearButton(View view){
        isCleared=true;
    }
}
