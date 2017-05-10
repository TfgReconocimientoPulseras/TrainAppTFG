package com.example.ivana.trainapptfg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class NoSensores extends Activity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_sensores);
    }

    protected void onResume() {

        super.onResume();
    }

    public void onClickButtonSalir(View view) {
        finish();
    }
}
