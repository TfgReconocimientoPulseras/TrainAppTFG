package com.example.ivana.trainapptfg.fragments;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ivana.trainapptfg.DataTAD;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.RecogerDatosBienvenida;
import com.example.ivana.trainapptfg.RecogerDatosRecogida;
import com.example.ivana.trainapptfg.miSensorEventListener;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import joinery.DataFrame;

public class ReconocerActividadFragment extends Fragment {
    private DataFrame df;
    private DataFrame featuresSegmentado1;
    private DataFrame featuresSegmentado2;

    private Handler modificadorActividad = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int f = (Integer)msg.obj;
            String texto = "";

            if(f == 0){
                texto = "No tengo muy claro lo que estás haciendo.";
                iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_act_0, 0, 0, 0);
            }
            else if(f == 1){
                texto = "Estas caminando.";
                iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_act_1, 0, 0, 0);
            }
            else if(f == 2){
                texto = "Estás barriendo.";
                iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_act_2, 0, 0, 0);
            }
            else if(f == 3){
                texto = "Estás de pie.";
                iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_act_3, 0, 0, 0);
            }
            else if(f == 4){
                texto = "Estas subiendo las escaleras.";
                iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_act_4, 0, 0, 0);
            }
            else if(f == 5){
                texto = "Estás bajando las escaleras.";
                iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_act_5, 0, 0, 0);
            }
            nombreActividad.setText(texto);
        }
    };

    private Collection colsNames = new ArrayList<String>(){{
        add("timestamp");
        add("gyro-alpha");
        add("gyro-beta");
        add("gyro-gamma");
        add("accel-x");
        add("accel-y");
        add("accel-z");
    }};

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

    //GESTION DE SENSORES////////////////////////////////////////////////////////////////////////////////////////////
    private SensorManager mSensorManager;
    private miSensorEventListener miSensorEventListenerAcelerometro;
    private miSensorEventListener miSensorEventListenerGiroscopio;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private static final int FREQUENCY_DEF = 100;
    private static final int DELAY_TIMER_TASK = 1000;
    private static final int WINDOW_SZ = 1000; //In miliseconds --> 1000 = 1s
    private static final int NUM_ACTIVIDADES = 5;
    private static final double PRECIOSION_ACEPTADA_ACIERTO = 0.5;
    //{"Andar", "Barrer", "De pie", "Subir escaleras", "Bajar escaleras"};

    private int dfUtilizado = 2;
    private int reconocedorEncendido = 0;


    private Timer timer;
    private TimerTask timerTask;
    private int timeAcumulated;

    //GESTION GRAFICA
    private Button button;
    private FloatingActionButton anadirActividad;
    private TextView nombreActividad;
    private TextView iconoActividad;

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
                float[] floatUnificada = DataTAD.concatenateValues(dataGyro.getValues(), dataAccel.getValues());
                DataTAD dataUnificada = new DataTAD(System.currentTimeMillis(), floatUnificada);

                int actividadPredicha = -1;

                //lo añadimos al dataframe
                df.append(dataUnificada.getDataTADasArrayList());

                if (timeAcumulated >= (5 * 1000)) { // tras 30 segundos para pruebas
                    //desactivarSensores();
                    //timer.cancel();

                    //SEGMENTAR DATOS DEL DATAFRAME (VENTANAS DE 1S Y SIN SOLAPAMIENTO)
                    // DataFrame features = segmentameDatosSinSolapamiento(df);

                    //2 -> SOLAPAMIENTO DEL 50%, 4-> SOLAPAMIENTO DEL 25%...
                    //SEGMENTACION CON SOLAPAMIENTO (VENTANAS DE 1S Y CON SOLAPAMIENTO)

                    //long startTime = System.currentTimeMillis();
                    if(dfUtilizado == 2) {
                        dfUtilizado = 1;
                        featuresSegmentado1 = segmentameDatosConSolapamiento(df, 2);
                        actividadPredicha = clasificarActividad(featuresSegmentado1);
                    }
                    else{
                        dfUtilizado = 2;
                        featuresSegmentado2 = segmentameDatosConSolapamiento(df, 2);
                        actividadPredicha = clasificarActividad(featuresSegmentado2);
                    }

                    df = new DataFrame(colsNames);
                    timeAcumulated = 0;

                    //long stopTime = System.currentTimeMillis();
                    //long elapsedTime = stopTime - startTime;
                    //System.out.println(elapsedTime);

                    //DESCOMENTAR PARA GUARDAR DATOS DE DF Y FEATURES EN FICHERO -> PARA DEPURACIÓN DE DATOS
                    //formatDataToCsvExternalStorage("DataSetSensores", df);
                    //formatDataToCsvExternalStorage("DataSetFeatures", featuresSegmentado);

                    //Ejecutar clasificador con los datos de features
                    //clasificarActividad(featuresSegmentado);
                    //BORRAR DF
                    //
                    Message msg = new Message();
                    msg.obj = actividadPredicha;
                    modificadorActividad.sendMessage(msg);

                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reconocer_actividad, container, false);

        nombreActividad = (TextView) view.findViewById(R.id.nombre_actividad);
        iconoActividad = (TextView) view.findViewById(R.id.icono_actividad);

        iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_pausa, 0, 0, 0);

        button = (Button) view.findViewById(R.id.boton_reconocer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(reconocedorEncendido == 0) {
                    //button.setEnabled(false);
                    reconocedorEncendido = 1;
                    nombreActividad.setText("Comenzando a reconocer...");
                    button.setText("Parar el reconocimiento");
                    activarSensores();
                    timer.scheduleAtFixedRate(timerTask, DELAY_TIMER_TASK, FREQUENCY_DEF);
                }
                else if(reconocedorEncendido == 1){
                    desactivarSensores();
                    timer.cancel();
                    reconocedorEncendido = 0;
                    nombreActividad.setText("Para volver a reconocer pulse el botón de abajo.");
                    button.setText("Comenzar a reconocer");
                    iconoActividad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_pausa, 0, 0, 0);
                }
            }
        });

        /*this.anadirActividad = (FloatingActionButton) view.findViewById(R.id.anadirAct);
        this.anadirActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recogida = new Intent(getActivity(), RecogerDatosBienvenida.class);
                startActivity(recogida);
            }
        });*/

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
        for (Pair p: list) {
            int i = (Integer)p.getKey();
            int f = (Integer)p.getValue();
            long tIni= (long) df.get(i, 0);
            long tFin = (long) df.get(f, 0);
            long diff = tFin - tIni;
            Log.d("PRUEBAS DE TIEMPOS", "tIni: " + tIni + " tFin: " + tFin + "Diff: " + diff);

        }

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
    //TODO ARREGLAR ORDEN
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

    private void formatDataToCsvExternalStorage(String fName, DataFrame df) {
        String fileName = new Date().getTime() + "_" + fName + ".csv";
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

    private int clasificarActividad(DataFrame df){
        int frecuenciaAparicion[] = new int[NUM_ACTIVIDADES];
        double max = 0;
        int posMax = 0;

        for (int i = 0; i < df.length(); i++) {
            frecuenciaAparicion[(getPredictClass(i, df) - 1)]++;
        }

        for (int i = 0; i < NUM_ACTIVIDADES; i++) {
            if (frecuenciaAparicion[i] > max) {
                max = frecuenciaAparicion[i];
                posMax = i;
            }
        }

        Log.d("RecogerDatos", "MAX: " + Double.toString(max));
        Log.d("RecogerDatos", "DF SIZE: " + Integer.toString(df.length()));
        Log.d("RecogerDatos", "FREQ: " + Double.toString(((Double)(max/df.length()))));
        for(int i = 0; i < NUM_ACTIVIDADES; i++){
            Log.d("RecogerDatos", "Actividad " + Integer.toString(i+1) + " " + Integer.toString(frecuenciaAparicion[i]));
        }
        Log.d("RecogerDatos", "-----------------------------------------");

        if((double)(max /df.length()) > PRECIOSION_ACEPTADA_ACIERTO) {
            return (posMax + 1);
        }
        else{
            return 0;
        }
    }

    private int getPredictClass(int i,DataFrame df){
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
