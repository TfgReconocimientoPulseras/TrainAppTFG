package com.example.ivana.trainapptfg.Services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.ivana.trainapptfg.DataTAD;
import com.example.ivana.trainapptfg.miSensorEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import joinery.DataFrame;

/**
 * Created by Iván on 27/04/2017.
 */

public class RecogidaDeDatosService extends Service{
    //NOMBRES PARA LOS DATAFRAMES////////////////////////////////////////////////////////////////////////////////////////////
    private Collection colsNames = new ArrayList<String>(){{
        add("timestamp");
        add("gyro-alpha");
        add("gyro-beta");
        add("gyro-gamma");
        add("accel-x");
        add("accel-y");
        add("accel-z");
    }};

    //GESTIÓN DE DATAFRAMES//////////////////////////////////////////////////////////////////////////////////////////////////
    private DataFrame df;

    //GESTIÓN DE SENSORES///////////////////////////////////////////////////////////////////////////////////////////////////
    private SensorManager mSensorManager;
    private miSensorEventListener miSensorEventListenerAcelerometro;
    private miSensorEventListener miSensorEventListenerGiroscopio;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    //GESTIÓN DE TIMER//////////////////////////////////////////////////////////////////////////////////////////////////////
    private Timer timer;
    private TimerTask timerTask;
    private int timeAcumulated;

    //CONSTANTES/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int FREQUENCY_DEF = 100; //100ms
    private static final int DELAY_TIMER_TASK = 3000; //1000ms


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        df = new DataFrame(colsNames);
        this.timer = new Timer();
        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.miSensorEventListenerAcelerometro = new miSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new miSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.timeAcumulated = 0;

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                timeAcumulated += FREQUENCY_DEF;

                DataTAD dataAccel = miSensorEventListenerAcelerometro.obtenerDatosSensor();
                DataTAD dataGyro = miSensorEventListenerGiroscopio.obtenerDatosSensor();

                //unificamos los valores del acelerómetro y del giroscopio...
                float[] floatUnificada = DataTAD.concatenateValues(dataGyro.getValues(), dataAccel.getValues());
                DataTAD dataUnificada = new DataTAD(System.currentTimeMillis(), floatUnificada);

                int actividadPredicha = -1;

                //lo añadimos al dataframe
                df.append(dataUnificada.getDataTADasArrayList());

                if (timeAcumulated >= (5 * 1000)) { // tras 5 segundos para pruebas

                    //TODO ENCONTRAR LA MANERA DE PASARLE AL SEGMENTACIÓN DE DATOS SERVICE EL DF
                    df = new DataFrame(colsNames);
                    Log.d("Servicio - Recogida!", "Ya han pasado 5 segundos\n");
                    timeAcumulated = 0;

                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags,
                              int startId) {
        activarSensores();
        timer.scheduleAtFixedRate(timerTask, DELAY_TIMER_TASK, FREQUENCY_DEF);
        return Service.START_STICKY;
    }

    private void activarSensores() {
        this.miSensorEventListenerAcelerometro.activarSensor();
        this.miSensorEventListenerGiroscopio.activarSensor();
    }

    private void desactivarSensores() {
        this.miSensorEventListenerAcelerometro.desactivarSensor();
        this.miSensorEventListenerGiroscopio.desactivarSensor();
    }

    //TODO ON DESTROY!!!!!! CUIDADO ¬¬

}
