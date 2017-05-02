package com.example.ivana.trainapptfg.Services;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

import joinery.DataFrame;

public class ClasificacionDeDatosThread implements Runnable {
    private final BlockingQueue<DataFrame> queueConsume;
    private final BlockingQueue<Integer> queueProduce;

    public ClasificacionDeDatosThread(BlockingQueue bqConsumeFrom, BlockingQueue bqProduceTo){
        this.queueConsume = bqConsumeFrom;
        this.queueProduce = bqProduceTo;
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
                e.printStackTrace();
            }
        }
    }
    private DataFrame consume(BlockingQueue<DataFrame> bq){
        DataFrame dfRet = null;

        try {
            dfRet = bq.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return dfRet;
    }

    private int produce(DataFrame df, int i){
        return getPredictClass(i,df);
    }

    private int getPredictClass(int i, DataFrame df){
        if((double)df.get(i, "accel_z_max") <= 0.270629882812)
            if((double)df.get(i, "accel_z_max") <= -0.0557861328125)
                if((double)df.get(i, "gyro_alpha_min") <= -10.234375)
                    if((double)df.get(i, "yz_cor") <= -0.265517234802)
                        if((double)df.get(i, "yz_cor") <= -0.722222208977)
                            if((double)df.get(i, "accel_x_med") <= 0.861938476562)
                                return 5;
                            else //if(accel_x_med > 0.861938476562)
                                return 1;
                        else //if(yz_cor > -0.722222208977)
                            return 1;
                    else //if(yz_cor > -0.265517234802)
                        if((double)df.get(i, "xz_cor") <= 0.00561797758564)
                            if((double)df.get(i, "xy_cor") <= -0.352941185236)
                                return 5;
                            else //if(xy_cor > -0.352941185236)
                                return 4;
                        else //if(xz_cor > 0.00561797758564)
                            if((double)df.get(i, "xz_cor") <= 0.122235879302)
                                return 5;
                            else //if(xz_cor > 0.122235879302)
                                return 4;
                else //if(gyro_alpha_min > -10.234375)
                    if((double)df.get(i, "gyro_alpha_avg") <= 7.396484375)
                        if((double)df.get(i, "gyro_gamma_max") <= 20.04296875)
                            if((double)df.get(i, "gyro_gamma_avg") <= -7.64794921875)
                                return 4;
                            else //if(gyro_gamma_avg > -7.64794921875)
                                return 3;
                        else //if(gyro_gamma_max > 20.04296875)
                            if((double)df.get(i, "yz_cor") <= -0.0770308151841)
                                return 1;
                            else //if(yz_cor > -0.0770308151841)
                                return 4;
                    else //if(gyro_alpha_avg > 7.396484375)
                        if((double)df.get(i, "yz_cor") <= -0.265517234802)
                            if((double)df.get(i, "yz_cor") <= -0.722222208977)
                                return 1;
                            else //if(yz_cor > -0.722222208977)
                                return 1;
                        else //if(yz_cor > -0.265517234802)
                            if((double)df.get(i, "accel_y_avg") <= 0.00970458984375)
                                return 3;
                            else //if(accel_y_avg > 0.00970458984375)
                                return 4;
            else //if(accel_z_max > -0.0557861328125)
                if((double)df.get(i, "accel_y_min") <= -0.216796875)
                    if((double)df.get(i, "accel_y_max") <= -0.186157226562)
                        if((double)df.get(i, "x_fft") <= 0.584477305412)
                            if((double)df.get(i, "gyro_beta_std") <= 17.2740154266)
                                return 1;
                            else //if(gyro_beta_std > 17.2740154266)
                                return 2;
                        else //if(x_fft > 0.584477305412)
                            return 1;
                    else //if(accel_y_max > -0.186157226562)
                        if((double)df.get(i, "yz_cor") <= -0.200900912285)
                            if((double)df.get(i, "accel_x_min") <= 0.76513671875)
                                return 1;
                            else //if(accel_x_min > 0.76513671875)
                                return 1;
                        else //if(yz_cor > -0.200900912285)
                            if((double)df.get(i, "accel_x_min") <= 0.76513671875)
                                return 5;
                            else //if(accel_x_min > 0.76513671875)
                                return 1;
                else //if(accel_y_min > -0.216796875)
                    if((double)df.get(i, "yz_cor") <= -0.265517234802)
                        if((double)df.get(i, "yz_cor") <= -0.722222208977)
                            if((double)df.get(i, "x_fft") <= 2.30511021614)
                                return 5;
                            else //if(x_fft > 2.30511021614)
                                return 4;
                        else //if(yz_cor > -0.722222208977)
                            return 1;
                    else //if(yz_cor > -0.265517234802)
                        if((double)df.get(i, "xz_cor") <= -0.0926301553845)
                            if((double)df.get(i, "xz_cor") <= -0.588235318661)
                                return 4;
                            else //if(xz_cor > -0.588235318661)
                                return 4;
                        else //if(xz_cor > -0.0926301553845)
                            if((double)df.get(i, "yz_cor") <= 0.063502676785)
                                return 5;
                            else //if(yz_cor > 0.063502676785)
                                return 4;
        else //if(accel_z_max > 0.270629882812)
            if((double)df.get(i, "accel_x_max") <= 1.220703125)
                if((double)df.get(i, "accel_z_min") <= 0.146728515625)
                    if((double)df.get(i, "y_fft") <= 0.333095878363)
                        if((double)df.get(i, "accel_y_std") <= 0.0845201537013)
                            return 5;
                        else //if(accel_y_std > 0.0845201537013)
                            return 2;
                    else //if(y_fft > 0.333095878363)
                        if((double)df.get(i, "gyro_beta_max") <= 158.6484375)
                            if((double)df.get(i, "gyro_beta_std") <= 0.832784533501)
                                return 1;
                            else //if(gyro_beta_std > 0.832784533501)
                                return 2;
                        else //if(gyro_beta_max > 158.6484375)
                            if((double)df.get(i, "accel_z_max") <= 0.718383789062)
                                return 1;
                            else //if(accel_z_max > 0.718383789062)
                                return 2;
                else //if(accel_z_min > 0.146728515625)
                    if((double)df.get(i, "gyro_alpha_std") <= 3.76972877802e-07)
                        if((double)df.get(i, "xz_cor") <= 0.142105266452)
                            return 1;
                        else //if(xz_cor > 0.142105266452)
                            return 2;
                    else //if(gyro_alpha_std > 3.76972877802e-07)
                        return 2;
            else //if(accel_x_max > 1.220703125)
                if((double)df.get(i, "accel_y_min") <= -0.153930664062)
                    return 1;
                else //if(accel_y_min > -0.153930664062)
                    if((double)df.get(i, "accel_z_avg") <= 0.469650268555)
                        if((double)df.get(i, "accel_x_max") <= 1.55456542969)
                            if((double)df.get(i, "gyro_gamma_max") <= 20.52734375)
                                return 5;
                            else //if(gyro_gamma_max > 20.52734375)
                                return 4;
                        else //if(accel_x_max > 1.55456542969)
                            return 5;
                    else //if(accel_z_avg > 0.469650268555)
                        return 2;

    }
}
