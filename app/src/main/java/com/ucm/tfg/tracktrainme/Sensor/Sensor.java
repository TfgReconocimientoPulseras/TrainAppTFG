package com.ucm.tfg.tracktrainme.Sensor;

import com.ucm.tfg.tracktrainme.Utilidades.DataTAD;

public interface Sensor {

    public void encenderSensor();

    public void apagarSensor();

    public DataTAD obtenerDatosAcel();

    public DataTAD obtenerDatosGyro();
}
