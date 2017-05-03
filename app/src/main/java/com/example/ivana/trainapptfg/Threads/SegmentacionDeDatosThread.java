package com.example.ivana.trainapptfg.Threads;

import android.util.Log;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import joinery.DataFrame;

public class SegmentacionDeDatosThread implements Runnable {

    private final BlockingQueue<DataFrame> queueConsume;
    private final BlockingQueue<DataFrame> queueProduce;
    //NOMBRES PARA LOS DATAFRAMES////////////////////////////////////////////////////////////////////////////////////////////

    private Collection featuresMinNames = new ArrayList<String>(){{
        add("gyro_alpha_min");
        add("gyro_beta_min");
        add("gyro_gamma_min");
        add("accel_x_min");
        add("accel_y_min");
        add("accel_z_min");
    }};

    private Collection featuresMaxNames = new ArrayList<String>(){{
        add("gyro_alpha_max");
        add("gyro_beta_max");
        add("gyro_gamma_max");
        add("accel_x_max");
        add("accel_y_max");
        add("accel_z_max");
    }};

    private Collection featuresMeanNames = new ArrayList<String>(){{
        add("gyro_alpha_avg");
        add("gyro_beta_avg");
        add("gyro_gamma_avg");
        add("accel_x_avg");
        add("accel_y_avg");
        add("accel_z_avg");
    }};

    private Collection featuresMedianNames = new ArrayList<String>(){{
        add("gyro_alpha_med");
        add("gyro_beta_med");
        add("gyro_gamma_med");
        add("accel_x_med");
        add("accel_y_med");
        add("accel_z_med");
    }};

    private Collection featuresStdNames = new ArrayList<String>(){{
        add("gyro_alpha_std");
        add("gyro_beta_std");
        add("gyro_gamma_std");
        add("accel_x_std");
        add("accel_y_std");
        add("accel_z_std");
    }};

    private Collection featuresCorrNames = new ArrayList<String>(){{
        add("xy_cor");
        add("xz_cor");
        add("yz_cor");
    }};

    private Collection featuresFftNames = new ArrayList<String>(){{
        add("x_fft");
        add("y_fft");
        add("z_fft");
    }};

    //CONSTANTES/////////////////////////////////////////////////////////////////
    private static final int WINDOW_SZ = 1000; //In miliseconds --> 1000 = 1s
    private DataFrame dfSegmentado;

    public SegmentacionDeDatosThread(BlockingQueue bqConsumeFrom, BlockingQueue bqProduceTo){
        this.queueConsume = bqConsumeFrom;
        this.queueProduce = bqProduceTo;
    }

    @Override
    public void run() {

        while (true) {
            try {
                DataFrame dfRawData = consume(queueConsume);
                Log.d("Segment_Thread", "He consumido un dataframe");
                queueProduce.put(produce(dfRawData));
                Log.d("Segment_Thread", "He producido un dataframeSegmentado");

            } catch (InterruptedException e) {
                Log.d("Thread - Segmentacion", "Interrupted");
                return;
            }
        }

    }

    private DataFrame produce(DataFrame df){
        return segmentameDatosConSolapamiento(df, 2);
    }

    private DataFrame consume(BlockingQueue<DataFrame> bq) throws InterruptedException {
        DataFrame dfRet = null;

        dfRet = bq.take();


        return dfRet;
    }

    private DataFrame segmentameDatosConSolapamiento(DataFrame df, int porcentajeSolapamiento){
        DataFrame retDf = new DataFrame();

        int timeOverlap = WINDOW_SZ/porcentajeSolapamiento;
        DataFrame dataFrameMin = new DataFrame(this.featuresMinNames);
        DataFrame dataFrameMax = new DataFrame(this.featuresMaxNames);
        DataFrame dataFrameMean = new DataFrame(this.featuresMeanNames);
        DataFrame dataFrameMedian = new DataFrame(this.featuresMedianNames);
        DataFrame dataFrameStd = new DataFrame(this.featuresStdNames);
        DataFrame dataFrameCorr = new DataFrame(this.featuresCorrNames);
        DataFrame dataFrameFft = new DataFrame(this.featuresFftNames);
        List<Pair<Integer,Integer>> list = new ArrayList<>();
        long timeStart = 0;
        int startSlice = 0;

        for (int row = 0; row < df.length(); row++){

            if(timeStart == 0){
                timeStart = (long) df.get(row, 0);
                startSlice = row;
            }
            else if((long) df.get(row, 0) >= (timeStart + WINDOW_SZ - 10) ){
                list.add(new Pair<Integer, Integer>(startSlice, row));
                row--;

                while((long) df.get(row,0) >= (timeStart + timeOverlap - 10))
                    row--;

                timeStart = 0;
            }

        }

        //si el timestart es distinto de 0 significa que hay valores que se han quedado en la ventana sin procesar,
        //ocurre cuando el número de datos que quedan es menor que el ancho de la ventana.
        if(timeStart != 0 && !df.isEmpty()){
            list.add(new Pair<Integer, Integer>(startSlice, df.length() - 1));

            int row = df.length() - 1;
            while((long) df.get(row,0) >= (timeStart + timeOverlap - 10))
                row--;

            list.add(new Pair<Integer, Integer>(row, df.length() - 1));

        }


        //TODO PARA PRUEBAS BORRAR CUANDO NO SEA NECESARIO
        /*
        for (Pair p: list) {
            int i = (Integer)p.getKey();
            int f = (Integer)p.getValue();
            long tIni= (long) df.get(i, 0);
            long tFin = (long) df.get(f, 0);
            long diff = tFin - tIni;
            Log.d("PRUEBAS DE TIEMPOS", "tIni: " + tIni + " tFin: " + tFin + "Diff: " + diff);

        }*/

        for (Pair p: list) {
            int i = (Integer)p.getKey();
            int f = (Integer)p.getValue();

            dataFrameMin.append(df.slice(i, f, 1, df.size()).min().row(0));
            dataFrameMax.append(df.slice(i, f, 1, df.size()).max().row(0));
            dataFrameMean.append(df.slice(i,f, 1, df.size()).mean().row(0));
            dataFrameMedian.append(df.slice(i, f, 1, df.size()).median().row(0));
            dataFrameStd.append(df.slice(i, f, 1, df.size()).stddev().row(0));
            dataFrameCorr.append(giveMeCorrelation(df.slice(i, f, 1, df.size())));
            dataFrameFft.append(giveMeFFT(df.slice(i, f, 1, df.size())));
        }

        //join de los dataframes
        return dataFrameMean.join(dataFrameMin).join(dataFrameMax).join(dataFrameStd).join(dataFrameCorr).join(dataFrameFft).join(dataFrameMedian);
    }


    /**
     * Por ahora esta funcion devuelve en una lista de valores, la correlación entre las variables accel-x accel - y | accel - x accel - z | accel - y accel - z
     * Es importante que en el dataframe este ordenado, ya que el acceso se realiza de manera manual
     * 2ºAccels (x, y, z)
     * 1ºGyros (a , b , g)
     *
     * @param df
     * @return
     */
    //PASAR SOLO EL DF DE LOS ACCEL
    private List giveMeCorrelation(DataFrame df){
        List retList = new ArrayList();

        double[][] miMatrix = (double[][]) df.toArray(double[][].class);
        RealMatrix rm = new PearsonsCorrelation(miMatrix).getCorrelationMatrix();
        double [][] matrixDebug = rm.getData();

        retList.add(matrixDebug[3][4]);
        retList.add(matrixDebug[3][5]);
        retList.add(matrixDebug[4][5]);

        return retList;
    }

    /**
     *Devuelve la energía de fourier de los acelerómetros
     * importante el orden del df
     * 1ºGyro
     * 2ºAcell
     * @param df
     * @return
     */
    private List giveMeFFT(DataFrame df){
        List retList = new ArrayList();
        //http://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/transform/FastFourierTransformer.html
        //http://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/transform/DftNormalization.html
        //http://jakevdp.github.io/blog/2013/08/28/understanding-the-fft/

        double[][] miMatrix = (double[][]) df.toArray(double[][].class);
        RealMatrix rm = new Array2DRowRealMatrix(miMatrix);
        int n = df.length();
        double logaritmo;
        double[] x = rm.getColumn(3);// accel-x
        double[] y = rm.getColumn(4);// accel-y
        double[] z = rm.getColumn(5);// accel-z

        if(!ArithmeticUtils.isPowerOfTwo(df.length())){
            logaritmo = Math.log(x.length)/Math.log(2);
            x = Arrays.copyOf(x, (int) Math.pow(2, Math.ceil(logaritmo)));
            y = Arrays.copyOf(y, (int) Math.pow(2, Math.ceil(logaritmo)));
            z = Arrays.copyOf(z, (int) Math.pow(2, Math.ceil(logaritmo)));
        }


        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        Complex[] X = fft.transform(x, TransformType.FORWARD);
        Complex[] Y = fft.transform(y, TransformType.FORWARD);
        Complex[] Z = fft.transform(z, TransformType.FORWARD);

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;

        //TODO PENDIENTE DE COMPROBACIÓN (MARLON)
        //TODO FOR HASTA df.length o X.length?
        for (int i = 0; i < df.length(); i++) {
            sumX += Math.pow(X[i].abs(), 2);
            sumY += Math.pow(Y[i].abs(), 2);
            sumZ += Math.pow(Z[i].abs(), 2);
        }

        //TODO PENDIENTE DE COMPROBACIÓN (MARLON)
        retList.add(sumX/df.length());
        retList.add(sumY/df.length());
        retList.add(sumZ/df.length());



        return retList;
    }
}
