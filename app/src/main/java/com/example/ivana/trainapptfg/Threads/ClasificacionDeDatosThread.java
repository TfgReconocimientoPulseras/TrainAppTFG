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
        if((double)df.get(i, "gyro_gamma_max") <= 0.288688659668)
            if((double)df.get(i, "gyro_gamma_avg") <= -0.0725975036621)
                if((double)df.get(i, "accel_x_med") <= 3.93776702881)
                    if((double)df.get(i, "accel_y_std") <= 0.334382146597)
                        if((double)df.get(i, "gyro_beta_max") <= 0.799121797085)
                            if((double)df.get(i, "accel_z_std") <= 0.439901858568)
                                return 3;
                            else //if(accel_z_std > 0.439901858568)
                                return 1;
                        else //if(gyro_beta_max > 0.799121797085)
                            if((double)df.get(i, "accel_x_avg") <= 3.07346057892)
                                return 2;
                            else //if(accel_x_avg > 3.07346057892)
                                return 1;
                    else //if(accel_y_std > 0.334382146597)
                        if((double)df.get(i, "accel_y_std") <= 0.670810461044)
                            if((double)df.get(i, "accel_y_max") <= -8.11328697205)
                                return 1;
                            else //if(accel_y_max > -8.11328697205)
                                return 2;
                        else //if(accel_y_std > 0.670810461044)
                            if((double)df.get(i, "accel_y_max") <= -0.670837402344)
                                return 2;
                            else //if(accel_y_max > -0.670837402344)
                                return 1;
                else //if(accel_x_med > 3.93776702881)
                    if((double)df.get(i, "gyro_gamma_avg") <= -0.256838232279)
                        if((double)df.get(i, "gyro_beta_avg") <= 1.19128644466)
                            if((double)df.get(i, "accel_y_std") <= 0.47832852602)
                                return 4;
                            else //if(accel_y_std > 0.47832852602)
                                return 4;
                        else //if(gyro_beta_avg > 1.19128644466)
                            if((double)df.get(i, "gyro_gamma_med") <= -1.28017807007)
                                return 1;
                            else //if(gyro_gamma_med > -1.28017807007)
                                return 2;
                    else //if(gyro_gamma_avg > -0.256838232279)
                        if((double)df.get(i, "gyro_gamma_max") <= 0.157119750977)
                            if((double)df.get(i, "gyro_alpha_std") <= 0.142421230674)
                                return 3;
                            else //if(gyro_alpha_std > 0.142421230674)
                                return 4;
                        else //if(gyro_gamma_max > 0.157119750977)
                            if((double)df.get(i, "xy_cor") <= -0.0170060098171)
                                return 2;
                            else //if(xy_cor > -0.0170060098171)
                                return 3;
            else //if(gyro_gamma_avg > -0.0725975036621)
                if((double)df.get(i, "accel_y_std") <= 0.324032753706)
                    return 3;
                else //if(accel_y_std > 0.324032753706)
                    if((double)df.get(i, "y_fft") <= 109.977348328)
                        if((double)df.get(i, "gyro_gamma_max") <= 0.0921401977539)
                            return 4;
                        else //if(gyro_gamma_max > 0.0921401977539)
                            return 1;
                    else //if(y_fft > 109.977348328)
                        if((double)df.get(i, "accel_y_std") <= 0.556065320969)
                            return 3;
                        else //if(accel_y_std > 0.556065320969)
                            return 2;
        else //if(gyro_gamma_max > 0.288688659668)
            if((double)df.get(i, "accel_y_avg") <= -8.46217155457)
                if((double)df.get(i, "accel_x_max") <= 7.61885070801)
                    if((double)df.get(i, "accel_y_max") <= -7.25489044189)
                        if((double)df.get(i, "gyro_gamma_min") <= -0.783500671387)
                            if((double)df.get(i, "gyro_alpha_max") <= 0.646278917789)
                                return 1;
                            else //if(gyro_alpha_max > 0.646278917789)
                                return 1;
                        else //if(gyro_gamma_min > -0.783500671387)
                            if((double)df.get(i, "accel_x_min") <= 1.68486702442)
                                return 2;
                            else //if(accel_x_min > 1.68486702442)
                                return 3;
                    else //if(accel_y_max > -7.25489044189)
                        if((double)df.get(i, "accel_z_avg") <= 3.68591809273)
                            if((double)df.get(i, "accel_x_avg") <= 2.43097448349)
                                return 2;
                            else //if(accel_x_avg > 2.43097448349)
                                return 1;
                        else //if(accel_z_avg > 3.68591809273)
                            if((double)df.get(i, "gyro_gamma_min") <= -1.37046051025)
                                return 4;
                            else //if(gyro_gamma_min > -1.37046051025)
                                return 2;
                else //if(accel_x_max > 7.61885070801)
                    if((double)df.get(i, "gyro_gamma_min") <= -1.48977661133)
                        if((double)df.get(i, "xz_cor") <= -0.0134727042168)
                            if((double)df.get(i, "accel_x_max") <= 8.62800598145)
                                return 4;
                            else //if(accel_x_max > 8.62800598145)
                                return 4;
                        else //if(xz_cor > -0.0134727042168)
                            if((double)df.get(i, "accel_x_med") <= 4.4800491333)
                                return 1;
                            else //if(accel_x_med > 4.4800491333)
                                return 4;
                    else //if(gyro_gamma_min > -1.48977661133)
                        if((double)df.get(i, "accel_y_min") <= -14.0640029907)
                            return 2;
                        else //if(accel_y_min > -14.0640029907)
                            if((double)df.get(i, "accel_x_std") <= 1.013027668)
                                return 2;
                            else //if(accel_x_std > 1.013027668)
                                return 1;
            else //if(accel_y_avg > -8.46217155457)
                if((double)df.get(i, "gyro_gamma_min") <= -1.53284239769)
                    if((double)df.get(i, "gyro_gamma_max") <= 2.01449012756)
                        if((double)df.get(i, "gyro_alpha_max") <= 1.3117338419)
                            if((double)df.get(i, "yz_cor") <= -0.701584339142)
                                return 4;
                            else //if(yz_cor > -0.701584339142)
                                return 4;
                        else //if(gyro_alpha_max > 1.3117338419)
                            if((double)df.get(i, "accel_z_min") <= -0.2079962641)
                                return 4;
                            else //if(accel_z_min > -0.2079962641)
                                return 2;
                    else //if(gyro_gamma_max > 2.01449012756)
                        if((double)df.get(i, "gyro_beta_min") <= -2.68766021729)
                            if((double)df.get(i, "gyro_gamma_med") <= 0.414778411388)
                                return 4;
                            else //if(gyro_gamma_med > 0.414778411388)
                                return 1;
                        else //if(gyro_beta_min > -2.68766021729)
                            if((double)df.get(i, "xz_cor") <= 0.740190625191)
                                return 4;
                            else //if(xz_cor > 0.740190625191)
                                return 4;
                else //if(gyro_gamma_min > -1.53284239769)
                    if((double)df.get(i, "accel_x_med") <= 4.7153339386)
                        if((double)df.get(i, "accel_y_min") <= -12.7256469727)
                            if((double)df.get(i, "accel_x_max") <= 3.74657869339)
                                return 2;
                            else //if(accel_x_max > 3.74657869339)
                                return 2;
                        else //if(accel_y_min > -12.7256469727)
                            if((double)df.get(i, "accel_z_med") <= 2.66837310791)
                                return 2;
                            else //if(accel_z_med > 2.66837310791)
                                return 2;
                    else //if(accel_x_med > 4.7153339386)
                        if((double)df.get(i, "accel_y_med") <= -3.95844578743)
                            if((double)df.get(i, "gyro_gamma_max") <= 1.25952076912)
                                return 4;
                            else //if(gyro_gamma_max > 1.25952076912)
                                return 4;
                        else //if(accel_y_med > -3.95844578743)
                            if((double)df.get(i, "gyro_gamma_max") <= 1.63673436642)
                                return 2;
                            else //if(gyro_gamma_max > 1.63673436642)
                                return 4;

    }
}
