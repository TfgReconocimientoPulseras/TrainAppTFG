package com.example.ivana.trainapptfg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * AQUI ESTÁN LOS FICHEROS QUE SE CREAN
 * C:\Users\ivana\AppData\Local\Android\sdk\platform-tools
 * adb shell
 * cd data/data/com.example.ivana.trainapptfg
 * cd files
 * ls -l
 **/

/**
 * Esta actividad se encarga de recoger datos y crear un fichero con los datos
 * <p>
 * Los ficheros se encuentran en la siguiente ubicación (Windows)
 * C:\Users\ivana\AppData\Local\Android\sdk\platform-tools
 * adb shell
 * cd data/data/com.example.ivana.trainapptfg
 * cd files
 * ls -l
 */
public class RecogerDatos extends AppCompatActivity {

    //ELEMENTOS GRÁFICOS/////////////////////////////////////////////////////////////////////////////////////////////
    private TextView mTextMessage;
    private EditText nameUserText;
    private EditText nameActivityText;
    private EditText timePerFileText;
    private EditText numberFilesText;
    private ProgressBar progressBar;
    private Button buttonRecord;
    private SeekBar seekBar;

    //PARAMETROS DE CONFIGURACION ACTIVIDADES////////////////////////////////////////////////////////////////////////
    private String nameUser;
    private String nameActivity;
    private int timePerFile;
    private int numberFiles;
    private int frequency;

    //GESTION DE SENSORES////////////////////////////////////////////////////////////////////////////////////////////
    private SensorManager mSensorManager;
    private miSensorEventListener miSensorEventListenerAcelerometro;
    private miSensorEventListener miSensorEventListenerGiroscopio;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    //ALMACENAMIENTO DATOS DE SENSORES///////////////////////////////////////////////////////////////////////////////
    private ArrayList<DataTAD> dataListAccel;
    private ArrayList<DataTAD> dataListGyro;
    private ArrayList<DataTAD> dataListSensores;

    //GESTION DEL TIEMPO PARA RECOGIDA DE DATOS POR SENSORES/////////////////////////////////////////////////////////
    private int timeAcumulated;
    private int numFileCreated;
    private Timer timer;

    //CONSTANTES/////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int NUM_ATRIB_ACCEL = 3;
    private static final int NUM_ATRIB_GYRO = 3;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 123;
    private static final String PATH_DATA_DIR =  Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles";
    private static final int FREQUENCY_DEF = 100;
    private static final String TAG = "RecogerDatos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        this.dataListAccel = new ArrayList<DataTAD>();
        this.dataListGyro = new ArrayList<DataTAD>();
        this.dataListSensores = new ArrayList<DataTAD>();

        this.miSensorEventListenerAcelerometro = new miSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new miSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);


        this.nameUserText = (EditText) findViewById(R.id.nameText);
        this.nameActivityText = (EditText) findViewById(R.id.activityText);
        this.timePerFileText = (EditText) findViewById(R.id.timeText);
        this.numberFilesText = (EditText) findViewById(R.id.filesText);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.buttonRecord = (Button) findViewById(R.id.buttonRecord);
        this.seekBar = (SeekBar) findViewById(R.id.seekBar);

        this.frequency = FREQUENCY_DEF;
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //TODO SE PODRÍA METER EN UN HASHMAP LOS VALORES DEL PROGRESS Y SU FREQ CORRESPONDIENDTE
                if (progress == 0) {
                    frequency = 50;
                } else if (progress == 1) {
                    frequency = 100;
                } else if (progress == 2) {
                    frequency = 200;
                } else if (progress == 3) {
                    frequency = 300;
                } else if (progress == 4) {
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

    public void onClickButtonPlay(View view) {
        this.nameUser = this.nameUserText.getText().toString();
        this.nameActivity = this.nameActivityText.getText().toString();
        this.timePerFile = Integer.parseInt(this.timePerFileText.getText().toString());
        this.numberFiles = Integer.parseInt(this.numberFilesText.getText().toString());
        this.timeAcumulated = 0;
        this.numFileCreated = 0;
        this.timer = new Timer();
        this.buttonRecord.setEnabled(false);
        this.progressBar.setProgress(0);

        this.activarSensores();

        this.progressBar.setMax((this.timePerFile * 1000) * numberFiles);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                incrementProgressBar(frequency);
                timeAcumulated += frequency;

                rellenaListasDeDatos();


                if (timeAcumulated >= (timePerFile * 1000)) {
                    comprobacionTimestamp();

                    if(numFileCreated >= numberFiles){
                        miSensorEventListenerAcelerometro.desactivarSensor();
                        miSensorEventListenerGiroscopio.desactivarSensor();
                        //limpiarFormulario();
                        timer.cancel();

                        //actualizaMemoria();
                    }
                    else{
                        timeAcumulated = 0;
                        numFileCreated++;

                        //Archivos con datos del sensor tipo:
                        formatDataToCsvExternalStorage(dataListAccel, 1);    //Sensor.TYPE_ACCELEROMETER
                        formatDataToCsvExternalStorage(dataListGyro, 2);     //Sensor.TYPE_GYROSCOPE
                        formatDataToCsvExternalStorage(dataListSensores, 3); //Sensor.TYPE_ALL
                    }
                }
            }
        };

        /*
         * Pasado 1s comienza a ejecutarse la tarea "timerTask" cada 100 ms
         */
        this.timer.scheduleAtFixedRate(timerTask, 1000, this.frequency);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterListener();
    }

    private void desactivarSensores() {
        this.miSensorEventListenerAcelerometro.desactivarSensor();
        this.miSensorEventListenerGiroscopio.desactivarSensor();
    }

    private void activarSensores() {
        this.miSensorEventListenerAcelerometro.activarSensor();
        this.miSensorEventListenerGiroscopio.activarSensor();
    }

    private void incrementProgressBar(int increment) {
        this.progressBar.incrementProgressBy(increment);
    }

    private void formatDataToCsvInternalStorage(ArrayList<DataTAD> list, int type) {
        String fileName = this.nameUser + "_" + this.nameActivity + "_" + String.valueOf(type) + ".csv";

        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(openFileOutput(fileName, 0));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (DataTAD dataItem : list) {
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

    private void formatDataToCsvExternalStorage(ArrayList<DataTAD> list, int type) {
        String fileName = new Date().getTime() + "_" + this.nameUser + "_" + this.nameActivity + "_" + String.valueOf(type) + ".csv";
        //File mySD = Environment.getExternalStorageDirectory();
        //File mySD = getExternalFilesDir(null);
        //File mySD2 = Environment.getDataDirectory();
        //File mySD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles");
        directory.mkdirs();
        File file = new File(directory, fileName);


        try {
            FileOutputStream fOs = new FileOutputStream(file);
            OutputStreamWriter oSw = new OutputStreamWriter(fOs);

            for (DataTAD dataItem : list) {
                String formatted = dataItem.formattedString() + "\n";
                oSw.write(formatted);
            }


            oSw.flush();
            oSw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessageToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    private void comprobacionTimestamp() {
        for (int i = 0; i < dataListAccel.size(); i++) {
            Log.d(TAG, "Diferencia entre TimeStamp: " + "Acel: " + dataListAccel.get(i).formattedString() + "\n" + " Gyro: " + dataListGyro.get(i).formattedString() + "\n" + " List: "  + dataListSensores.get(i).formattedString() + "\n" + " Diff: " + String.valueOf(this.dataListAccel.get(i).getTimestamp() - this.dataListGyro.get(i).getTimestamp()) + " Diff2: " + String.valueOf(this.dataListSensores.get(i).getTimestamp() - this.dataListGyro.get(i).getTimestamp()));
        }
    }

    private void rellenaListasDeDatos() {
        DataTAD dAccel = miSensorEventListenerAcelerometro.obtenerDatosSensor();
        DataTAD dGyro = miSensorEventListenerGiroscopio.obtenerDatosSensor();

        if (dAccel.getValues() != null && dGyro.getValues() != null) {
            //long timeInMillis = System.currentTimeMillis();
            long timeInMillis = System.currentTimeMillis();
            dataListAccel.add(dAccel);
            dataListGyro.add(dGyro);

            float[] aux = new float[NUM_ATRIB_ACCEL + NUM_ATRIB_GYRO];
            System.arraycopy(dAccel.getValues(), 0, aux, 0, NUM_ATRIB_ACCEL);
            System.arraycopy(dGyro.getValues(), 0, aux, NUM_ATRIB_ACCEL, NUM_ATRIB_GYRO);
            //TODO VIGILAR ESTE PARÁMETRO (VA 10 MIN ADELANTADO A LOS OTROS)
            //long timeInMillis = (new Date()).getTime()
             //       + (event.timestamp - System.nanoTime()) / 1000000L;
            dataListSensores.add(new DataTAD(timeInMillis, aux));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //MIRAR QUE SE ESTÉ REALIZANDO BIEN
    private void actualizaMemoria(){
        MediaScannerConnection.scanFile(this, new String[]{PATH_DATA_DIR}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                //showMessageToast("hola");
            }
        });
    }

    private void limpiarFormulario(){
        timePerFileText.setText("");
        numberFilesText.setText("");
        buttonRecord.setEnabled(true);
    }
}
