package com.example.ivana.trainapptfg.Utilidades;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class miSensorEventListener implements SensorEventListener{

    private float[] values;
    private long timestamp;
    private Sensor sensor;
    private SensorManager mSensorManager;
    private int frecuencia;

    public miSensorEventListener(Sensor sensor, SensorManager sm, int freq) {
        this.sensor = sensor;
        this.mSensorManager = sm;
        this.frecuencia = freq;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        this.values = event.values;
        this.timestamp = (System.currentTimeMillis() - SystemClock.elapsedRealtime()) + (event.timestamp/1000000L);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerMiSensorListener(){
        this.mSensorManager.registerListener(this, this.sensor, this.frecuencia);
    }

    private void desregistrarMiSensorListener(){
        this.mSensorManager.unregisterListener(this);
    }

    public void activarSensor(){
        this.registerMiSensorListener();
    }

    public void desactivarSensor(){
        this.desregistrarMiSensorListener();
    }

    public DataTAD obtenerDatosSensor(){
        return new DataTAD(this.timestamp, this.values);
    }
}
