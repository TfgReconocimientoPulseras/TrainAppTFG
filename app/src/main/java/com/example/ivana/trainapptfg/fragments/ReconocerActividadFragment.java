package com.example.ivana.trainapptfg.fragments;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ivana.trainapptfg.DataTAD;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.RecogerDatosBienvenida;
import com.example.ivana.trainapptfg.Utils;
import com.example.ivana.trainapptfg.miSensorEventListener;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.MathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarOutputStream;

import joinery.DataFrame;

import static android.R.id.list;

public class ReconocerActividadFragment extends Fragment {
    private DataFrame df;

    private Collection colsNames = new ArrayList<String>(){{
        add("timestamp");
        add("ax");
        add("ay");
        add("az");
        add("ga");
        add("gb");
        add("gg");
    }};

    private Collection featuresMinNames = new ArrayList<String>(){{
        add("min-ax");
        add("min-ay");
        add("min-az");
        add("min-ga");
        add("min-gb");
        add("min-gg");
    }};

    private Collection featuresMaxNames = new ArrayList<String>(){{
        add("max-ax");
        add("max-ay");
        add("max-az");
        add("max-ga");
        add("max-gb");
        add("max-gg");
    }};

    private Collection featuresMeanNames = new ArrayList<String>(){{
        add("mean-ax");
        add("mean-ay");
        add("mean-az");
        add("mean-ga");
        add("mean-gb");
        add("mean-gg");
    }};

    private Collection featuresMedianNames = new ArrayList<String>(){{
        add("median-ax");
        add("median-ay");
        add("median-az");
        add("median-ga");
        add("median-gb");
        add("median-gg");
    }};

    private Collection featuresStdNames = new ArrayList<String>(){{
        add("std-ax");
        add("std-ay");
        add("std-az");
        add("std-ga");
        add("std-gb");
        add("std-gg");
    }};

    private Collection featuresCorrNames = new ArrayList<String>(){{
        add("xy");
        add("xz");
        add("yz");
    }};

    private Collection featuresFftNames = new ArrayList<String>(){{
        add("x_fft");
        add("y_fft");
        add("z_fft");
    }};

    //GESTION DE SENSORES////////////////////////////////////////////////////////////////////////////////////////////
    private SensorManager mSensorManager;
    private miSensorEventListener miSensorEventListenerAcelerometro;
    private miSensorEventListener miSensorEventListenerGiroscopio;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private static final int FREQUENCY_DEF = 100;
    private static final int DELAY_TIMER_TASK = 1000;
    private static final int WINDOW_SZ = 1000; //In miliseconds --> 1000 = 1s



    private Timer timer;
    private TimerTask timerTask;
    private int timeAcumulated;

    private Button button;
    private FloatingActionButton anadirActividad;

    public ReconocerActividadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.df = new DataFrame(colsNames);
        this.mSensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        this.miSensorEventListenerAcelerometro = new miSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new miSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.timeAcumulated = 0;

        //TODO PASADOS X SEGUNDOS PARAR EL TIMERTASK Y EJECUTAR TAREAS DE EXTRACCIÓN DE CARACTERÍSTICAS Y CLASIFICAR DATOS
        this.timer = new Timer();

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                timeAcumulated += FREQUENCY_DEF;


                DataTAD dataAccel = miSensorEventListenerAcelerometro.obtenerDatosSensor();
                DataTAD dataGyro = miSensorEventListenerGiroscopio.obtenerDatosSensor();

                //unificamos los valores del acelerómetro y del giroscopio...
                float[] floatUnificada = DataTAD.concatenateValues(dataAccel.getValues(), dataGyro.getValues());
                DataTAD dataUnificada = new DataTAD(System.currentTimeMillis(), floatUnificada);

                //lo añadimos al dataframe
                df.append(dataUnificada.getDataTADasArrayList());

                if (timeAcumulated >= (30 * 1000)) { // tras 30 segundos para pruebas
                    desactivarSensores();
                    timer.cancel();

                    //SEGMENTAR DATOS DEL DATAFRAME (VENTANAS DE 1S Y SIN SOLAPAMIENTO)
                    //DataFrame features = segmentameDatosSinSolapamiento(df);

                    //2 -> SOLAPAMIENTO DEL 50%, 4-> SOLAPAMIENTO DEL 25%...
                    //SEGMENTACION CON SOLAPAMIENTO (VENTANAS DE 1S Y CON SOLAPAMIENTO)

                    //long startTime = System.currentTimeMillis();
                    DataFrame featuresSegmentado = segmentameDatosConSolapamiento(df, 2);

                    //long stopTime = System.currentTimeMillis();
                    //long elapsedTime = stopTime - startTime;
                    //System.out.println(elapsedTime);
                    formatDataToCsvExternalStorage("sensorDataset", df);
                    formatDataToCsvExternalStorage("feautresDataset", featuresSegmentado);
                    int kk = 0;
                    //Ejecutar clasificador con los datos de features
                    //TODO INVOCAR AL CÓDIGO DEL ÁRBOL


                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reconocer_actividad, container, false);
        button = (Button) view.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                activarSensores();
                timer.scheduleAtFixedRate(timerTask, DELAY_TIMER_TASK, FREQUENCY_DEF);
            }
        });


        this.anadirActividad = (FloatingActionButton) view.findViewById(R.id.anadirAct);
        this.anadirActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recogida = new Intent(getActivity(), RecogerDatosBienvenida.class);
                startActivity(recogida);
            }
        });

        return view;
    }


    private void activarSensores() {
        this.miSensorEventListenerAcelerometro.activarSensor();
        this.miSensorEventListenerGiroscopio.activarSensor();
    }

    private void desactivarSensores() {
        this.miSensorEventListenerAcelerometro.desactivarSensor();
        this.miSensorEventListenerGiroscopio.desactivarSensor();
    }

    /*
    //VENTANA DESLIZANTE 1 SEGUNDO SIN SOLAPAMIENTO
    private DataFrame segmentameDatosSinSolapamiento(DataFrame df){
        DataFrame retDf = new DataFrame();

        DataFrame dataFrameMin = new DataFrame(this.feautresMinNames);
        DataFrame dataFrameMax = new DataFrame(this.feautresMaxNames);

        long timeStart = 0;
        int startSlice = 0;

        for (int row = 0; row < df.length(); row++){
            if(timeStart == 0){
                timeStart = (long) df.get(row, 0);
                startSlice = row;
            }

            if((long) df.get(row, 0) >= timeStart + WINDOW_SZ ){
                dataFrameMin.append(df.slice(startSlice,  row, 1, df.size()).min().row(0));
                dataFrameMax.append(df.slice(startSlice,  row, 1, df.size()).max().row(0));
                timeStart = 0;
            }
        }

        //si el timestart es distinto de 0 significa que hay valores que se han quedado en la ventana sin procesar,
        //ocurre cuando el número de datos que quedan es menor que el ancho de la ventana.
        if(timeStart != 0 && !df.isEmpty()){
            dataFrameMin.append(df.slice(startSlice,  df.length(), 1, df.size()).min().row(0));
            dataFrameMax.append(df.slice(startSlice,  df.length(), 1, df.size()).max().row(0));
        }

        //join de los dataframes
        return  dataFrameMin.join(dataFrameMax);
    }
*/
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

        long timeStart = 0;
        int startSlice = 0;

        for (int row = 0; row < df.length(); row++){
            if(timeStart == 0){
                timeStart = (long) df.get(row, 0);
                startSlice = row;
            }

            if((long) df.get(row, 0) >= timeStart + WINDOW_SZ ){
                dataFrameMin.append(df.slice(startSlice,  row, 1, df.size()).min().row(0));
                dataFrameMax.append(df.slice(startSlice,  row, 1, df.size()).max().row(0));
                dataFrameMean.append(df.slice(startSlice,  row, 1, df.size()).mean().row(0));
                dataFrameMedian.append(df.slice(startSlice,  row, 1, df.size()).median().row(0));
                dataFrameStd.append(df.slice(startSlice,  row, 1, df.size()).stddev().row(0));
                dataFrameCorr.append(giveMeCorrelation(df.slice(startSlice,  row, 1, df.size())));
                dataFrameFft.append(giveMeFFT(df.slice(startSlice,  row, 1, df.size())));

                //volver atrás para realizar el solapamiento
                while ((long) df.get(row, 0) >= timeStart + timeOverlap)
                    row--;

                //un row-- extra por el row++ del bucle
                row--;
                timeStart = 0;
            }
        }

        //si el timestart es distinto de 0 significa que hay valores que se han quedado en la ventana sin procesar,
        //ocurre cuando el número de datos que quedan es menor que el ancho de la ventana.
        if(timeStart != 0 && !df.isEmpty()){
            dataFrameMin.append(df.slice(startSlice,  df.length(), 1, df.size()).min().row(0));
            dataFrameMax.append(df.slice(startSlice,  df.length(), 1, df.size()).max().row(0));
            dataFrameMean.append(df.slice(startSlice,  df.length(), 1, df.size()).mean().row(0));
            dataFrameMedian.append(df.slice(startSlice,  df.length(), 1, df.size()).median().row(0));
            dataFrameStd.append(df.slice(startSlice,  df.length(), 1, df.size()).stddev().row(0));
            dataFrameCorr.append(giveMeCorrelation(df.slice(startSlice,  df.length(), 1, df.size())));
            dataFrameFft.append(giveMeFFT(df.slice(startSlice,  df.length(), 1, df.size())));
        }

        //join de los dataframes
        return  dataFrameMin.join(dataFrameMax).join(dataFrameMean).join(dataFrameMedian).join(dataFrameStd).join(dataFrameCorr).join(dataFrameFft);
    }




    /**
     * Por ahora esta funcion devuelve en una lista de valores, la correlación entre las variables accel-x accel - y | accel - x accel - z | accel - y accel - z
     * Es importante que en el dataframe este ordenado, ya que el acceso se realiza de manera manual
     * 1ºAccels (x, y, z)
     * 2ºGyros (a , b , g)
     *
     * @param df
     * @return
     */
    //TODO COMO SOLO CALCULAMOS CORRELACION DE LOS ACELERÓMETROS PASAR SOLO EL DATAFRAME CON LOS ACELEROMETROS
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

    private List giveMeFFT(DataFrame df){
        List retList = new ArrayList();
        //http://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/transform/FastFourierTransformer.html
        //http://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/transform/DftNormalization.html
        //http://jakevdp.github.io/blog/2013/08/28/understanding-the-fft/

        double[][] miMatrix = (double[][]) df.toArray(double[][].class);
        RealMatrix rm = new Array2DRowRealMatrix(miMatrix);

        double[] x = rm.getColumn(0);
        double[] y = rm.getColumn(1);
        double[] z = rm.getColumn(2);
        double logaritmo = Math.log(x.length)/Math.log(2);

        if(!Utils.isInteger(logaritmo)){
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
        for (int i = 0; i < X.length; i++) {
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

    private void formatDataToCsvExternalStorage(String fName, DataFrame df) {
        String fileName = new Date().getTime() + fName + ".csv";
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles");
        directory.mkdirs();
        File file = new File(directory, fileName);

        try {
            OutputStream oS = new FileOutputStream(file);
            df.writeCsv(oS);
            oS.flush();
            oS.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
