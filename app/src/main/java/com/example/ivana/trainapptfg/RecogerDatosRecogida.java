package com.example.ivana.trainapptfg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

//TODO SE PUEDE MODIFICAR LAS LISTAS A DATAFRAMES (JOINERY)
public class RecogerDatosRecogida extends Activity {

    //ELEMENTOS GRÁFICOS/////////////////////////////////////////////////////////////////////////////////////////////
    private ProgressBar progressBar;
    private Button buttonRecord;
    private TextView temporizador;
    private TextView numArchivosCreados;

    //PARAMETROS DE CONFIGURACION ACTIVIDADES////////////////////////////////////////////////////////////////////////
    private String nameUser;
    private String nameActivity;

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
    private static final int NUM_ARCHIVOS_CREAR = 3;
    private static final int TIEMPO_POR_ARCHIVO = 30000;

    private Handler modificadorFinalizador = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            buttonRecord.setEnabled(true);


            if((Integer)msg.obj == (NUM_ARCHIVOS_CREAR - 1))
                createSimpleDialog("¡Explendido! Ya casi estamos acabando. Vamos a repetir la prueba por última vez.");
            else if((Integer)msg.obj == NUM_ARCHIVOS_CREAR) {
                createSimpleDialog("¡Genial! Hemos acabado.");
                numFileCreated = 0;
                Intent recogida = new Intent (RecogerDatosRecogida.this, RecogerDatosBienvenida.class);
                startActivity(recogida);
            }
            else
                createSimpleDialog("¡Muy bien! Vamos a volver a repetir la prueba. Vuelve a pulsar el botón de play.");

            numArchivosCreados.setText(Integer.toString((Integer)msg.obj) + "/" + Integer.toString(NUM_ARCHIVOS_CREAR));


        }
    };

    private Handler modificadorTemporizador = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int t = Integer.parseInt(String.valueOf(temporizador.getText()));

            if(t != 0){
                t--;
                if(t < 10){
                    temporizador.setText("0" + Integer.toString(t));
                }
                else {
                    temporizador.setText(Integer.toString(t));
                }
            }
        }
    };

    public void notificationAviso() {
        Intent i = new Intent(this, this.getClass());
        i.putExtra("notificationID", 1);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        CharSequence contentTitle = "Grabando actividad";
        CharSequence contentText = "Hemos terminado una parte. Podemos continuar.";
        Notification noti = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ico_activ)
                .setVibrate(new long[] {100, 250, 100, 500})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        nm.notify(1, noti);
    }

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos_recogida);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.miSensorEventListenerAcelerometro = new miSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new miSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);

        this.dataListAccel = new ArrayList<DataTAD>();
        this.dataListGyro = new ArrayList<DataTAD>();
        this.dataListSensores = new ArrayList<DataTAD>();

        Bundle bundle = getIntent().getExtras();
        this.nameUser = bundle.getString("nombreUsu");
        this.nameActivity = bundle.getString("nombreAct");

        this.numArchivosCreados = (TextView) findViewById(R.id.numArchivosCreadosRecogida);
        this.temporizador = (TextView) findViewById(R.id.temporizadorRecogida);
        this.buttonRecord = (Button) findViewById(R.id.buttonPlayRecogida);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBarRecogida);

        this.numFileCreated = 0;

        temporizador.setText(Integer.toString(TIEMPO_POR_ARCHIVO/1000));
    }

    protected void onResume() {

        super.onResume();
    }

    public void onClickButtonPlay(View view) {
        this.timeAcumulated = 0;

        this.timer = new Timer();

        this.buttonRecord.setEnabled(false);

        this.progressBar.setProgress(0);

        this.activarSensores();

        this.progressBar.setMax((this.TIEMPO_POR_ARCHIVO));

        temporizador.setText(Integer.toString(TIEMPO_POR_ARCHIVO/1000));

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                incrementProgressBar(FREQUENCY_DEF);
                timeAcumulated += FREQUENCY_DEF;

                rellenaListasDeDatos();

                if((timeAcumulated % 1000) == 0){
                    modificadorTemporizador.sendEmptyMessage(0);
                }

                if (timeAcumulated >= (TIEMPO_POR_ARCHIVO)) {
                    comprobacionTimestamp();

                    miSensorEventListenerAcelerometro.desactivarSensor();
                    miSensorEventListenerGiroscopio.desactivarSensor();

                    timer.cancel();
                    formatDataToCsvExternalStorage(dataListAccel, 1);    //Sensor.TYPE_ACCELEROMETER
                    formatDataToCsvExternalStorage(dataListGyro, 2);     //Sensor.TYPE_GYROSCOPE
                    formatDataToCsvExternalStorage(dataListSensores, 3); //Sensor.TYPE_ALL

                    numFileCreated++;

                    Message msg = new Message();
                    msg.obj = numFileCreated;
                    modificadorFinalizador.sendMessage(msg);

                   /* Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    v.vibrate(3000);
                  */
                    notificationAviso();
                }
            }
        };

        /*
         * Pasado 1s comienza a ejecutarse la tarea "timerTask" cada 100 ms
         */
        this.timer.scheduleAtFixedRate(timerTask, 1000, this.FREQUENCY_DEF);
    }

    public void createSimpleDialog(String texto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Importante");
        builder.setMessage(texto);
        builder.setPositiveButton("OK",null);
        builder.create();
        builder.show();
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
        long timeInMillis = System.currentTimeMillis();

        if (dAccel.getValues() != null && dGyro.getValues() != null) {
            dataListAccel.add(dAccel);
            dataListGyro.add(dGyro);

            float[] aux = new float[NUM_ATRIB_ACCEL + NUM_ATRIB_GYRO];
            System.arraycopy(dAccel.getValues(), 0, aux, 0, NUM_ATRIB_ACCEL);
            System.arraycopy(dGyro.getValues(), 0, aux, NUM_ATRIB_ACCEL, NUM_ATRIB_GYRO);
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
}
