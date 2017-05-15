package com.example.ivana.trainapptfg.DataBase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ivan on 15/05/2017.
 */

public class ActivityDataTransfer {
    private long id;
    private String name;
    private Date fechaCreacion;

    public ActivityDataTransfer(long id, String name, Date fechaCreacion) {
        this.id = id;
        this.name = name;
        this.fechaCreacion = fechaCreacion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return "Actividad: " + getName() + "\n"
                + "Fecha creación: " + dateFormat.format(getFechaCreacion());
    }
}
