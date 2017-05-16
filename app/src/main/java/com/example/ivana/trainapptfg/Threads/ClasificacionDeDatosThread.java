package com.example.ivana.trainapptfg.Threads;

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
        return getPredictClass(i,df);
    }

    private int getPredictClass(int i, DataFrame df){
        if((double)df.get(i, "gyro_gamma_max") <= 0.209456175566)
            if((double)df.get(i, "accel_y_std") <= 0.242084830999)
                if((double)df.get(i, "gyro_alpha_min") <= -0.308585643768)
                    if((double)df.get(i, "accel_x_max") <= 9.31309700012)
                        if((double)df.get(i, "gyro_beta_std") <= 0.167273283005)
                            return 3;
                        else //if(gyro_beta_std > 0.167273283005)
                            if((double)df.get(i, "gyro_alpha_min") <= -0.643096804619)
                                return 3;
                            else //if(gyro_alpha_min > -0.643096804619)
                                return 1;
                    else //if(accel_x_max > 9.31309700012)
                        if((double)df.get(i, "gyro_gamma_med") <= -0.250750094652)
                            return 4;
                        else //if(gyro_gamma_med > -0.250750094652)
                            return 2;
                else //if(gyro_alpha_min > -0.308585643768)
                    if((double)df.get(i, "gyro_beta_min") <= -1.05088806152)
                        return 1;
                    else //if(gyro_beta_min > -1.05088806152)
                        return 3;
            else //if(accel_y_std > 0.242084830999)
                if((double)df.get(i, "accel_y_max") <= 2.62441110611)
                    if((double)df.get(i, "accel_x_med") <= 2.50304412842)
                        if((double)df.get(i, "gyro_gamma_min") <= -0.18482208252)
                            return 1;
                        else //if(gyro_gamma_min > -0.18482208252)
                            if((double)df.get(i, "accel_y_med") <= -3.60224342346)
                                return 4;
                            else //if(accel_y_med > -3.60224342346)
                                return 1;
                    else //if(accel_x_med > 2.50304412842)
                        if((double)df.get(i, "accel_y_std") <= 0.506831645966)
                            if((double)df.get(i, "gyro_gamma_med") <= -0.263118743896)
                                return 1;
                            else //if(gyro_gamma_med > -0.263118743896)
                                return 3;
                        else //if(accel_y_std > 0.506831645966)
                            if((double)df.get(i, "gyro_beta_max") <= 1.56587266922)
                                return 4;
                            else //if(gyro_beta_max > 1.56587266922)
                                return 1;
                else //if(accel_y_max > 2.62441110611)
                    return 2;
        else //if(gyro_gamma_max > 0.209456175566)
            if((double)df.get(i, "accel_y_avg") <= -8.48324775696)
                if((double)df.get(i, "accel_x_max") <= 8.04502105713)
                    if((double)df.get(i, "accel_y_min") <= -10.8130435944)
                        if((double)df.get(i, "accel_x_med") <= 4.60125732422)
                            if((double)df.get(i, "gyro_gamma_min") <= -0.466049194336)
                                return 1;
                            else //if(gyro_gamma_min > -0.466049194336)
                                return 1;
                        else //if(accel_x_med > 4.60125732422)
                            if((double)df.get(i, "gyro_beta_min") <= -1.39317393303)
                                return 1;
                            else //if(gyro_beta_min > -1.39317393303)
                                return 4;
                    else //if(accel_y_min > -10.8130435944)
                        if((double)df.get(i, "gyro_gamma_min") <= -0.682512402534)
                            if((double)df.get(i, "accel_z_avg") <= 3.95836639404)
                                return 1;
                            else //if(accel_z_avg > 3.95836639404)
                                return 4;
                        else //if(gyro_gamma_min > -0.682512402534)
                            if((double)df.get(i, "accel_x_max") <= 3.54990386963)
                                return 3;
                            else //if(accel_x_max > 3.54990386963)
                                return 1;
                else //if(accel_x_max > 8.04502105713)
                    if((double)df.get(i, "accel_x_med") <= 4.50099945068)
                        if((double)df.get(i, "yz_cor") <= 0.531381249428)
                            if((double)df.get(i, "accel_x_min") <= 0.725691199303)
                                return 4;
                            else //if(accel_x_min > 0.725691199303)
                                return 1;
                        else //if(yz_cor > 0.531381249428)
                            return 4;
                    else //if(accel_x_med > 4.50099945068)
                        if((double)df.get(i, "accel_x_max") <= 8.52625274658)
                            if((double)df.get(i, "xz_cor") <= -0.193183273077)
                                return 4;
                            else //if(xz_cor > -0.193183273077)
                                return 1;
                        else //if(accel_x_max > 8.52625274658)
                            return 4;
            else //if(accel_y_avg > -8.48324775696)
                if((double)df.get(i, "accel_y_max") <= 4.24551773071)
                    if((double)df.get(i, "accel_x_max") <= 8.47423171997)
                        if((double)df.get(i, "gyro_gamma_min") <= -1.3512878418)
                            if((double)df.get(i, "gyro_beta_max") <= 2.33505249023)
                                return 4;
                            else //if(gyro_beta_max > 2.33505249023)
                                return 1;
                        else //if(gyro_gamma_min > -1.3512878418)
                            if((double)df.get(i, "accel_x_med") <= 5.54896402359)
                                return 1;
                            else //if(accel_x_med > 5.54896402359)
                                return 4;
                    else //if(accel_x_max > 8.47423171997)
                        if((double)df.get(i, "accel_y_med") <= 0.981155395508)
                            if((double)df.get(i, "gyro_gamma_max") <= 1.2589943409)
                                return 4;
                            else //if(gyro_gamma_max > 1.2589943409)
                                return 4;
                        else //if(accel_y_med > 0.981155395508)
                            if((double)df.get(i, "z_fft") <= 161.493133545)
                                return 3;
                            else //if(z_fft > 161.493133545)
                                return 1;
                else //if(accel_y_max > 4.24551773071)
                    if((double)df.get(i, "accel_y_med") <= 0.325078427792)
                        if((double)df.get(i, "xy_cor") <= -0.523706614971)
                            return 4;
                        else //if(xy_cor > -0.523706614971)
                            return 2;
                    else //if(accel_y_med > 0.325078427792)
                        return 2;

    }
}
