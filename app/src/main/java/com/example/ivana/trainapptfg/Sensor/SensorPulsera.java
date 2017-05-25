package com.example.ivana.trainapptfg.Sensor;

import android.app.Activity;
import android.app.Service;

import com.example.ivana.trainapptfg.Services.BluetoothLeService;
import com.example.ivana.trainapptfg.Utilidades.DataTAD;

public class SensorPulsera implements Sensor{

    private BluetoothLeService mService;

    public SensorPulsera(BluetoothLeService ser){
        this.mService = ser;
    }

    @Override
    public void encenderSensor() {
        mService.mensaje_encenderSensorCC2650();
    }

    @Override
    public void apagarSensor() {
        mService.mensaje_apagarSensorCC2650();
    }

    @Override
    public DataTAD obtenerDatosAcel() {
        return null;
    }

    @Override
    public DataTAD obtenerDatosGyro() {
        return null;
    }

}
