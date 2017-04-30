package com.example.ivana.trainapptfg;

import android.os.Binder;

import joinery.DataFrame;

/**
 * Created by Iván on 29/04/2017.
 */

public class DataFrameWrapperBinder extends Binder {
    private DataFrame df;

    public DataFrameWrapperBinder(DataFrame data) {
        df = data;
    }

    public DataFrame getData() {
        return df;
    }
}
