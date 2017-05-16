package com.example.ivana.trainapptfg.EstadosFSM;

import com.example.ivana.trainapptfg.DataBase.HistoryDataTransfer;

public class EstadoActividad extends Estado {
    private int actividad;

    private HistoryDataTransfer historyDataTransfer;

    public EstadoActividad(int actividad){
        super();
        this.actividad = actividad;
    }

    @Override
    public Estado procesarActividad(int numeroActividad) {
        Estado estadoSiguiente;

        if(numeroActividad != this.actividad){

            estadoSiguiente = new EstadoDudoso(this, numeroActividad);
        }
        else{
            estadoSiguiente = this;
        }

        return estadoSiguiente;
    }

    public int getActividad() {
        return actividad;
    }
}
