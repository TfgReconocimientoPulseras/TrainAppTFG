package com.example.ivana.trainapptfg.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Services.RecogidaDeDatosService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import joinery.DataFrame;

public class ReconocerActividadFragment extends Fragment {


    private Handler modificadorActividad = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int f = (Integer)msg.obj;
            String texto = "";

            if(f == 0){
                texto = "No tengo muy claro lo que estás haciendo.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_0);
            }
            else if(f == 1){
                texto = "Estas caminando.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_1);
            }
            else if(f == 2){
                texto = "Estás aplaudiendo.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_8);
            }
            else if(f == 3){
                texto = "Estás quieto.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_3);
            }
            else if(f == 4){
                texto = "Estás barriendo.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_2);
            }
            nombreActividad.setText(texto);
        }
    };

    //GESTIÓN DE DATAFRAMES//////////////////////////////////////////////////////////////////////////////////////////////////
    private DataFrame featuresSegmentado1;
    private DataFrame featuresSegmentado2;

    // {"Andar", "Barrer", "De pie", "Subir escaleras", "Bajar escaleras"};

    private int reconocedorEncendido = 0;


    //GESTIÓN ELEMENTOS GRÁFICOS
    private Button button;
    private TextView nombreActividad;
    private ImageView iconoActividad;

    //BROADCAST RECEIVER
    private BroadcastReceiver receiver;

    public ReconocerActividadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reconocer_actividad, container, false);

        nombreActividad = (TextView) view.findViewById(R.id.nombre_actividad);
        iconoActividad = (ImageView) view.findViewById(R.id.icono_actividad);
        iconoActividad.setBackgroundResource(R.drawable.ico_pausa);

        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                int estado = intent.getIntExtra("estado", 0);
                Message msg = new Message();
                msg.obj = estado;
                modificadorActividad.sendMessage(msg);
            }
        };
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
                    iconoActividad.setBackgroundResource(R.drawable.ico_pausa);
                }
            }
        });

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

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((receiver),
                new IntentFilter("estado_actualizado")
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        super.onStop();
    }
}
