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
import android.widget.TextView;

import com.example.ivana.trainapptfg.DataTAD;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Services.RecogidaDeDatosService;
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

import static java.lang.Thread.sleep;

public class ReconocerActividadFragment extends Fragment {


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

    //GESTIÓN DE DATAFRAMES//////////////////////////////////////////////////////////////////////////////////////////////////
    private DataFrame featuresSegmentado1;
    private DataFrame featuresSegmentado2;


    //CONSTANTES/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int FREQUENCY_DEF = 100;
    //{"Andar", "Barrer", "De pie", "Subir escaleras", "Bajar escaleras"};

    private int dfUtilizado = 2;
    private int reconocedorEncendido = 0;


    //GESTIÓN ELEMENTOS GRÁFICOS
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

        /*
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


                    Message msg = new Message();
                    msg.obj = actividadPredicha;
                    modificadorActividad.sendMessage(msg);

                }
            }
        };
        */
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
            private Intent intent;
            @Override
            public void onClick(View v) {

                if(reconocedorEncendido == 0) {
                    reconocedorEncendido = 1;
                    nombreActividad.setText("Comenzando a reconocer...");
                    button.setText("Parar el reconocimiento");
                    intent = new Intent(getContext(), RecogidaDeDatosService.class);
                    getActivity().startService(intent);
                }
                else if(reconocedorEncendido == 1){
                    getActivity().stopService(intent);
                    nombreActividad.setText("Para volver a reconocer pulse el botón de abajo.");
                    button.setText("Comenzar a reconocer");
                    reconocedorEncendido = 0;
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
}
