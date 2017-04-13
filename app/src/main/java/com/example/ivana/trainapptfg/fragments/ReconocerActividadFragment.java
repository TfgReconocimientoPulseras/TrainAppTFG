package com.example.ivana.trainapptfg.fragments;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ivana.trainapptfg.DataTAD;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.RecogerDatosBienvenida;
import com.example.ivana.trainapptfg.miSensorEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import joinery.DataFrame;

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

                if (timeAcumulated >= (15 * 1000)) { // tras 5 segundos de momento para pruebas
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

        }

        //join de los dataframes
        return  dataFrameMin.join(dataFrameMax).join(dataFrameMean).join(dataFrameMedian).join(dataFrameStd);
    }

}
