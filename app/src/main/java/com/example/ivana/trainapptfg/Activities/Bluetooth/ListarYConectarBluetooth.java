package com.example.ivana.trainapptfg.Activities.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.ivana.trainapptfg.R;
//https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
public class ListarYConectarBluetooth extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;

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

        //COMPROBACION PARA VER SI EL DISPOSITIVO ES COMPATIBLE CON BLE, EN OTRO CASO MOSTRAR QUE NO LO ES
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //OBRTENEMOS EL SERVICIO DE BLUETOOTH Y EL ADAPTADOR
        this.bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();

        //COMPROBAMOS QUE ESTÉ ACTIVADO EL BLUETOOTH
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

}
