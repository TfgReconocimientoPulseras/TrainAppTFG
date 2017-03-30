package com.example.ivana.trainapptfg;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by ivana on 21/03/2017.
 */

public class DataTAD {
    private long timestamp; //In milliseconds
    private float[] values; //Values of sensor

    public DataTAD(long timestamp, float[] values) {
        this.timestamp = timestamp;
        this.values = values;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    public float[] getValues() {

        return values;
    }

    public void setValues(float[] values) {

        this.values = values;
    }

    public String formattedString(){
        ArrayList<String> miArrayString = new ArrayList<>();
        String ret;

        for (float f:this.values) {
            miArrayString.add(String.valueOf(f));
        }

        ret = this.timestamp + TextUtils.join(",", miArrayString);

        return ret;
    }
}
