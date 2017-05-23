package com.example.ivana.trainapptfg.EstadosFSM;

import com.example.ivana.trainapptfg.DataBase.HistoryDataTransfer;

import java.util.Date;

public class EstadoActividad extends Estado {
    private int actividad;


    public EstadoActividad(int actividad){
        super();
        this.actividad = actividad;
        historyDataTransfer = new HistoryDataTransfer(this.actividad, new Date(), null);
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

    public HistoryDataTransfer getHistoryDataTransfer(){
        return historyDataTransfer;
    }

}
