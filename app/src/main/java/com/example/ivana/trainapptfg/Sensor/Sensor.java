package com.example.ivana.trainapptfg.Sensor;

import com.example.ivana.trainapptfg.Utilidades.DataTAD;

public interface Sensor {

    public void encenderSensor();

    public void apagarSensor();

    public DataTAD obtenerDatosAcel();

    public DataTAD obtenerDatosGyro();
}
