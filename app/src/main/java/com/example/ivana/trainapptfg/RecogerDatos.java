package com.example.ivana.trainapptfg;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/** AQUI ESTÁN LOS FICHEROS QUE SE CREAN
 * C:\Users\ivana\AppData\Local\Android\sdk\platform-tools
 * adb shell
 * cd data/data/com.example.ivana.trainapptfg
 * cd files
 * ls -l
 **/

/**
 * Esta actividad se encarga de recoger datos y crear un fichero con los datos
 *
 * Los ficheros se encuentran en la siguiente ubicación (Windows)
 * C:\Users\ivana\AppData\Local\Android\sdk\platform-tools
 * adb shell
 * cd data/data/com.example.ivana.trainapptfg
 * cd files
 * ls -l
 */
public class RecogerDatos extends AppCompatActivity {

    private TextView mTextMessage;
    private EditText nameUserText;
    private EditText nameActivityText;
    private EditText timePerFileText;
    private EditText numberFilesText;
    private ProgressBar progressBar;
    private Button buttonRecord;
    private SeekBar seekBar;


    private String nameUser;
    private String nameActivity;
    private int timePerFile;
    private int numberFiles;
    private int frequency;

    private SensorManager mSensorManager;
    private miSensor miSensorAcelerometro;
    private miSensor miSensorGiroscopio;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private ArrayList<DataTAD> dataListAccel;
    private ArrayList<DataTAD> dataListGyro;
    private ArrayList<DataTAD> dataListSensores;

    private static final String TAG = "RecogerDatos";

    private int timeAcumulated;
    private Timer timer;

    static final int NUM_ATRIB_ACCEL = 3;
    static final int NUM_ATRIB_GYRO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos);

        this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);


        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        this.dataListAccel = new ArrayList<DataTAD>();
        this.dataListGyro = new ArrayList<DataTAD>();
        this.dataListSensores = new ArrayList<DataTAD>();

        /*this.mSensorListenerAccel = new SensorEventListener() {
            private long initialTime = 0;
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                long timeInMillis = (new Date()).getTime()
                        + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;
                dataListAccel.add(new DataTAD(timeInMillis, sensorEvent.values));
                if(this.initialTime == 0){
                    this.initialTime = timeInMillis;
                }
                if (timeInMillis - this.initialTime > timePerFile*1000){
                    unregisterListener(this);
                    Log.d(TAG, "Length: " + dataListAccel.size());
                    Log.d(TAG, "Timestamp: " + timeInMillis + " Sensor:" + sensorEvent.sensor.getName() + " X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        this.mSensorListenerGyro = new SensorEventListener() {
            private long initialTime = 0;
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                long timeInMillis = (new Date()).getTime()
                        + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;
                dataListGyro.add(new DataTAD(timeInMillis, sensorEvent.values));
                if(initialTime == 0){
                    initialTime = timeInMillis;
                }
                if (timeInMillis - initialTime > timePerFile*1000){
                    unregisterListener(this);
                    Log.d(TAG, "Length: " + dataListGyro.size());
                    Log.d(TAG, "Timestamp: " + timeInMillis + " Sensor:" + sensorEvent.sensor.getName() + " X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        */

        this.miSensorAcelerometro = new miSensor(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorGiroscopio = new miSensor(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);


        this.nameUserText = (EditText) findViewById(R.id.nameText);
        this.nameActivityText = (EditText) findViewById(R.id.activityText);
        this.timePerFileText = (EditText) findViewById(R.id.timeText);
        this.numberFilesText = (EditText) findViewById(R.id.filesText);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.buttonRecord = (Button) findViewById(R.id.buttonRecord);
        this.seekBar = (SeekBar) findViewById(R.id.seekBar);

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //TODO SE PODRÍA METER EN UN HASHMAP LOS VALORES DEL PROGRESS Y SU FREQ CORRESPONDIENDTE
                if(progress == 0){
                    frequency = 50;
                }
                else if(progress == 1){
                    frequency = 100;
                }
                else if(progress == 2){
                    frequency = 200;
                }
                else if(progress == 3){
                    frequency = 300;
                }
                else if(progress == 4){
                    frequency = 400;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        this.timeAcumulated = 0;
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
        this.timeAcumulated = 0;
        this.timer = new Timer();
        this.buttonRecord.setEnabled(false);

        this.activarSensores();

        this.progressBar.setMax(this.timePerFile * 1000);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                incrementProgressBar(100);
                timeAcumulated += 100;

                dataListAccel.add(miSensorAcelerometro.obtenerDatosSensor());
                dataListGyro.add(miSensorGiroscopio.obtenerDatosSensor());

                float[] aux = new float[NUM_ATRIB_ACCEL + NUM_ATRIB_GYRO];
                System.arraycopy(miSensorAcelerometro.obtenerDatosSensor().getValues(), 0, aux, 0, NUM_ATRIB_ACCEL);
                System.arraycopy(miSensorGiroscopio.obtenerDatosSensor().getValues(), 0, aux, NUM_ATRIB_ACCEL, NUM_ATRIB_GYRO);
                long timeInMillis = (new Date()).getTime();

                dataListSensores.add(new DataTAD(timeInMillis, aux));



                if(timeAcumulated >= (timePerFile * 1000)) {
                    miSensorAcelerometro.desactivarSensor();
                    miSensorGiroscopio.desactivarSensor();

                    comprobacionTimestamp();

                    timer.cancel();

                    //Escritura en ficheros
                    formatDataToCsv(Sensor.TYPE_ALL);
                    formatDataToCsv(Sensor.TYPE_ACCELEROMETER);
                    formatDataToCsv(Sensor.TYPE_GYROSCOPE);
                }
            }
        };

        /*
         * Cada segundo ejecutamos el TimerTask
         */
        this.timer.schedule(timerTask, 0l, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterListener();
    }

    private void desactivarSensores(){
        //mSensorManager.unregisterListener(listener);
        this.miSensorAcelerometro.desactivarSensor();
        this.miSensorGiroscopio.desactivarSensor();
    }

    private void activarSensores(){
        //TODO Posibilidad de implementar con un HANDLER
        //mSensorManager.registerListener(mSensorListenerAccel, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorAcelerometro.activarSensor();
        //mSensorManager.registerListener(mSensorListenerGyro, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorGiroscopio.activarSensor();
    }

    private void incrementProgressBar(int increment){
        this.progressBar.incrementProgressBy(increment);
    }
    private void formatDataToCsv(int type){
        String fileName = this.nameUser + "_" + this.nameActivity + "_" + String.valueOf(type);
        int size = 0;


        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(openFileOutput(fileName, 0));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(type == Sensor.TYPE_ACCELEROMETER){
            size = this.dataListAccel.size();
        }
        else if(type == Sensor.TYPE_GYROSCOPE){
            size = this.dataListGyro.size();
        }
        else if(type == Sensor.TYPE_ALL){
            size = Math.min(this.dataListAccel.size(), this.dataListGyro.size());
        }

        for (int i = 0; i < size; i++){
            DataTAD dataItem = dataListAccel.get(i);

            String formatted = dataItem.formattedString() + "\n";

            try {
                out.write(formatted);
            } catch (Throwable t) {
                showMessageToast("Error writing data");
            }

        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showMessageToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    private void comprobacionTimestamp(){
        for(int i = 0; i < dataListAccel.size(); i++){
            Log.d(TAG, "Diferencia entre TimeStamp: " + "Acel: " + dataListAccel.get(i).formattedString() + " Gyro: " + dataListGyro.get(i).formattedString() + " Diff: " + String.valueOf(this.dataListAccel.get(i).getTimestamp() - this.dataListGyro.get(i).getTimestamp()));
        }
    }
}