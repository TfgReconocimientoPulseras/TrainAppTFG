package com.ucm.tfg.tracktrainme.DataBase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityDataTransfer {
    private long id;
    private String name;
    private Date fechaCreacion;

    private String urlImage;

    public ActivityDataTransfer(long id, String name, Date fechaCreacion, String url) {
        this.id = id;
        this.name = name;
        this.fechaCreacion = fechaCreacion;
        this.urlImage = url;
    }

    public  ActivityDataTransfer(String name, Date fechaCreacion, String url){
        this.name = name;
        this.fechaCreacion = fechaCreacion;
        this.urlImage = url;
    }

    public  ActivityDataTransfer(String name, String url){
        this.name = name;
        this.urlImage = url;
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

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    @Override
    public String toString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return "Actividad: " + getName() + "\n"
                + "Fecha creación: " + dateFormat.format(getFechaCreacion());
    }
}
