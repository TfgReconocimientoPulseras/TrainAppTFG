package com.example.ivana.trainapptfg;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import joinery.DataFrame;

public class ReconocerActividad extends Activity {

    private DataFrame df;

    private Collection colsNames = new ArrayList<String>(){{
        add("timestamp");
        add("ax");
        add("ay");
        add("az");
        //add("ga");
        //add("gb");
        //add("gg");
    }};

    private Collection feautresMinNames = new ArrayList<String>(){{
        add("min-ax");
        add("min-ay");
        add("min-az");
        //add("min-ga");
        //add("min-gb");
        //add("min-gg");
    }};

    private Collection feautresMaxNames = new ArrayList<String>(){{
        add("max-ax");
        add("max-ay");
        add("max-az");
        //add("max-ga");
        //add("max-gb");
        //add("max-gg");
    }};

    private Collection coll;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconocer_actividad);
        this.button = (Button) findViewById(R.id.button1);

        this.df = new DataFrame(colsNames);

        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        this.miSensorEventListenerAcelerometro = new miSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new miSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.timeAcumulated = 0;

        //TODO PASADOS X SEGUNDOS PARAR EL TIMERTASK Y EJECUTAR TAREAS DE EXTRACCIÓN DE CARACTERÍSTICAS Y CLASIFICAR DATOS
        //TODO INCLUIR DATOS DEL GIROSOPIO
        this.timer = new Timer();

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                timeAcumulated += FREQUENCY_DEF;


                DataTAD dataAccel = miSensorEventListenerAcelerometro.obtenerDatosSensor();
                //DataTAD dataGyro = miSensorEventListenerGiroscopio.obtenerDatosSensor();

                df.append(dataAccel.getDataTADasArrayList());

                if (timeAcumulated >= (5 * 1000)) { // tras 5 segundos de momento para pruebas
                    desactivarSensores();
                    timer.cancel();

                    //SEGMENTAR DATOS DEL DATAFRAME (VENTANAS DE 1S Y SIN SOLAPAMIENTO)
                    DataFrame features = segmentameDatosSinSolapamiento(df);

                    //2 -> SOLAPAMIENTO DEL 50%, 4-> SOLAPAMIENTO DEL 25%...
                    //SEGMENTACION CON SOLAPAMIENTO (VENTANAS DE 1S Y CON SOLAPAMIENTO)
                    DataFrame featuresSegmentado = segmentameDatosConSolapamiento(df, 2);

                    int kk = 0;
                    //Ejecutar clasificador con los datos de features
                    //TODO INVOCAR AL CÓDIGO DEL ÁRBOL



                }
            }
        };
    }

    public void onClickButtonRun(View view) {
        button.setEnabled(false);
        this.activarSensores();
        this.timer.scheduleAtFixedRate(timerTask, DELAY_TIMER_TASK, FREQUENCY_DEF);
    }

    private void activarSensores() {
        this.miSensorEventListenerAcelerometro.activarSensor();
        this.miSensorEventListenerGiroscopio.activarSensor();
    }

    private void desactivarSensores() {
        this.miSensorEventListenerAcelerometro.desactivarSensor();
        this.miSensorEventListenerGiroscopio.desactivarSensor();
    }

    //VENTANA DESLIZANTE 1 SEGUNDO SIN SOLAPAMIENTO
    //TODO IMPLEMENTAR OTRA FUNCION CON SOLAPAMIENTO DE 50%
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

    //TODO COMPROBAR QUE HAGA CORRECTAMENTE LA SEGMENTACIÓN CON EL SOLAPAMIENTO
    private DataFrame segmentameDatosConSolapamiento(DataFrame df, int porcentajeSolapamiento){
        DataFrame retDf = new DataFrame();
        int timeOverlap = WINDOW_SZ/porcentajeSolapamiento;
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
        }

        //join de los dataframes
        return  dataFrameMin.join(dataFrameMax);
    }
}
