package com.example.ivana.trainapptfg.DataBase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ivan on 15/05/2017.
 */

public class HistoryDataTransfer {
    private long id;
    private int actividad;
    private Date fIni;
    private Date fFin;

    public String getNombreActividad() {
        return nombreActividad;
    }

    public void setNombreActividad(String nombreActividad) {
        this.nombreActividad = nombreActividad;
    }

    private String nombreActividad;

/*
            long diffInMilliSec = fFin.getTime() - fIni.getTime();

            long seconds = (diffInMilliSec / 1000) % 60;

            long minutes = (diffInMilliSec / (1000 * 60)) % 60;

            long hours = (diffInMilliSec / (1000 * 60 * 60)) % 24;*/

    public HistoryDataTransfer() {

    }


    public HistoryDataTransfer(int actividad, Date fIni, Date fFin) {
        this.actividad = actividad;
        this.fIni = fIni;
        this.fFin = fFin;
    }

    public HistoryDataTransfer(long id, int actividad, Date fIni, Date fFin) {
        this.id = id;
        this.actividad = actividad;
        this.fIni = fIni;
        this.fFin = fFin;
    }
    public HistoryDataTransfer(long id, int actividad, Date fIni, Date fFin, String nombreActividad) {
        this.id = id;
        this.actividad = actividad;
        this.fIni = fIni;
        this.fFin = fFin;
        this.nombreActividad = nombreActividad;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getActividad() {
        return actividad;
    }

    public void setActividad(int actividad) {
        this.actividad = actividad;
    }

    public Date getfIni() {
        return fIni;
    }

    public void setfIni(Date fIni) {
        this.fIni = fIni;
    }

    public Date getfFin() {
        return fFin;
    }

    public void setfFin(Date fFin) {
        this.fFin = fFin;
    }

    @Override
    public String toString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return "Actividad: " + nombreActividad +  "\n"
                + "tIni: " + dateFormat.format(this.fIni) + "\n"
                + "tFin: " + dateFormat.format(this.fFin);
    }
}
