package com.ucm.tfg.tracktrainme.Threads;

import android.content.Context;
import android.util.Log;

import com.ucm.tfg.tracktrainme.DataBase.DatabaseAdapter;
import com.ucm.tfg.tracktrainme.DataBase.TreeDataTransfer;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import bsh.EvalError;
import bsh.Interpreter;
import joinery.DataFrame;

public class ClasificacionDeDatosThread implements Runnable {
    private final BlockingQueue<DataFrame> queueConsume;
    private final BlockingQueue<Integer> queueProduce;
    private Interpreter interpreter;
    private String mArbol;

    public ClasificacionDeDatosThread(BlockingQueue bqConsumeFrom, BlockingQueue bqProduceTo, Context ctx){
        this.queueConsume = bqConsumeFrom;
        this.queueProduce = bqProduceTo;
        this.interpreter = new Interpreter();
        //TODO ALMACENAR STRING DE ARBOL EN LA BASE DE DATOS PARA ACCEDER POSTERIORMENTE
        //TODO REALIZAR FLUJO CON BASE DE DATOS DE ARBOLES
        //TODO ACCEDER AL ULTIMO ARBOL CREADO EN LA BASE DE DATOS
        DatabaseAdapter db = new DatabaseAdapter(ctx);
        TreeDataTransfer treeDataTransfer = null;
        db.open();
        treeDataTransfer = db.getLastTree();
        db.close();
        this.mArbol = treeDataTransfer.getTree();
        try {
            interpreter.eval(this.mArbol);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }

    }
    @Override
    public void run() {
        while (true) {
            try {
                DataFrame dfSegmentData = consume(queueConsume);
                Log.d("Segment_Clas", "He consumido un dataframe");

                for (int i = 0; i < dfSegmentData.length(); i++){
                    queueProduce.put(produce(dfSegmentData, i));
                    Log.d("Segment_Thread", "He producido " + (i + 1) + " datos clasificados");
                }

            } catch (InterruptedException e) {
                Log.d("Thread - Clasificacion", "Interrupted");
                return;
            }
        }
    }
    private DataFrame consume(BlockingQueue<DataFrame> bq) throws InterruptedException {
        DataFrame dfRet = null;

        dfRet = bq.take();

        return dfRet;
    }

    private int produce(DataFrame df, int i){
        return getPredictClass(df);
    }

    private int getPredictClass(DataFrame df){
        Integer prediccion = -2;

        try {
            HashMap hashMap = dataframeToHashMap(df);
            interpreter.set("hashMap", hashMap);
            prediccion = (Integer)interpreter.eval("miMetodo(hashMap)");

        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }

        return prediccion;
    }

    private HashMap dataframeToHashMap(DataFrame df){
        Set<String> list = df.columns();
        HashMap hashMap = new HashMap<>();

        for (String s: list){
            hashMap.put(s, df.get(0, s));
        }

        return hashMap;
    }
}
