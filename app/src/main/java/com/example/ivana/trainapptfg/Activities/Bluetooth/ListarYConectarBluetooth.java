package com.example.ivana.trainapptfg.Activities.Bluetooth;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.ivana.trainapptfg.R;

public class ListarYConectarBluetooth extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_yconectar_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO EDITAR ESTA VISTA PARA QUE SALGA UN LISTADO DE LOS DISPOSITIVOS BLUETOOTH CERCANOS Y QUE AL HACER "PULL TO REFRESH ACTUALICE LA LISTA"
        //https://git.ti.com/sensortag-20-android/sensortag-20-android/blobs/master/sensortag20/BleSensorTag/src/main/java/com/example/ti/ble/sensortag/MainActivity.java
        //import com.example.ti.ble.common.BleDeviceInfo;
        //import com.example.ti.ble.common.BluetoothLeService;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
