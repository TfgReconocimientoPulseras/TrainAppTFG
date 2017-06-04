package com.example.ivana.trainapptfg.EstadosFSM;

import com.example.ivana.trainapptfg.DataBase.DatabaseAdapter;

import java.util.Date;

public class EstadoDudoso extends Estado {
    private int contGlobal;
    private int contUltAct;
    private int ultAct;

    private Estado estadoAnterior;
    private int contActAnterior;

    public EstadoDudoso(Estado estadoAnterior, int ultAct){
        super();
        this.estadoAnterior = estadoAnterior;

        this.contGlobal = 1;
        this.contUltAct = 1;

        this.ultAct = ultAct;
        this.contActAnterior = 1;
    }

    public Estado procesarActividad(int numeroActividad) {
        Estado retEstado = this;


        if(contGlobal < Estado.MAX_SIZE){
            if(numeroActividad == ultAct){
                contUltAct++;
            }
            else{
                contUltAct = 1;
                ultAct = numeroActividad;
            }
        }

        //TODO MODIFICAR ESTO PARA QUE LLEGUEN MÁS LETRAS SEGUIDAS PARA QUE HAYA QUE CAMBIAR DE ACTIVIDAD, EN CAMBIO SI LLEGAN DE LA MISMA ACTIVIDAD
        //TODO SI NO VOLVER AL ESTADO NO ENTIENDO
        else if(contGlobal >= Estado.MAX_SIZE){
            DatabaseAdapter db = new DatabaseAdapter(getContext());
            db.open();
            historyDataTransfer.setfFin(new Date());
            long id = db.insertarNuevoRegistroAlHistorial(historyDataTransfer);
            db.close();

            retEstado = new EstadoNoTeEntiendo(this.contUltAct, this.ultAct);

        }

        if(contUltAct == 3 && estadoAnterior.getActividad() == ultAct){
            retEstado = estadoAnterior;
        }
        else if(contUltAct == 3 && estadoAnterior.getActividad() != ultAct){
            DatabaseAdapter db = new DatabaseAdapter(getContext());

            db.open();
            historyDataTransfer.setfFin(new Date());
            long id = db.insertarNuevoRegistroAlHistorial(historyDataTransfer);
            db.close();

            retEstado = new EstadoActividad(this.ultAct);
        }

        contGlobal++;

        return retEstado;
    }

    @Override
    public int getActividad() {
        return -2;
    }
}
