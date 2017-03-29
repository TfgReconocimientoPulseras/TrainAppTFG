package com.example.ivana.trainapptfg;

/**
 * Created by ivana on 21/03/2017.
 */

public class DataTAD {
    private long timestamp; //In milliseconds
    private float[] values; //Values of accelerometer

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

    public float[] getGyroscope() {
        return values;
    }

    public void setGyroscope(float[] gyroscope) {
        this.values = gyroscope;
    }

    public String formattedStringValues(){
        return String.valueOf(this.values[0]) + "," + String.valueOf(this.values[1]) + "," + String.valueOf(this.values[2]);
    }

    public String formattedStringTimestamp(){
        return String.valueOf(this.timestamp);
    }
}
