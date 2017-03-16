package com.example.ivana.trainapptfg;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

public class RecogerDatos extends AppCompatActivity implements SensorEventListener {

    private TextView mTextMessage;
    private EditText nameUserText;
    private EditText nameActivityText;
    private EditText timePerFileText;
    private EditText numberFilesText;
    private int frequency;

    private String nameUser;
    private String nameActivity;
    private int timePerFile;
    private int numberFiles;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;


    private static final String TAG = "RecogerDatos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos);

        this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);


        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        this.mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String nameSensor = sensorEvent.sensor.getName();

                long timeInMillis = (new Date()).getTime()
                        + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;

                Log.d(TAG, "Timestamp: " + timeInMillis + " Sensor:" + sensorEvent.sensor.getName() + " X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        this.nameUserText = (EditText) findViewById(R.id.nameText);
        this.nameActivityText = (EditText) findViewById(R.id.activityText);
        this.timePerFileText = (EditText) findViewById(R.id.timeText);
        this.numberFilesText = (EditText) findViewById(R.id.filesText);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onClickButtonPlay(View view){
        this.nameUser =  this.nameUserText.getText().toString();
        this.nameActivity = this.nameActivityText.getText().toString();
        this.timePerFile = Integer.parseInt(this.timePerFileText.getText().toString());
        this.numberFiles = Integer.parseInt(this.numberFilesText.getText().toString());

        this.registerListener();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                unregisterListener();
            }
        }, timePerFile);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterListener();
    }

    private void unregisterListener(){
        mSensorManager.unregisterListener(this.mSensorListener);
    }

    private void registerListener(){
        mSensorManager.registerListener(this.mSensorListener, mAccelerometer, 100000);
        mSensorManager.registerListener(this.mSensorListener, mGyroscope, 100000);

        //SALE GYROSCOPE
        //SALE GYROSCOPE
        //SALE GYROSCOPE
        //SALE ACCEL
        //SALE GYROSCOPE
        //SALE ACCEL
    }
}
