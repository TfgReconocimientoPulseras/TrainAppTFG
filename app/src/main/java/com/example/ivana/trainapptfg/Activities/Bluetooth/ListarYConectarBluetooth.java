package com.example.ivana.trainapptfg.Activities.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ivana.trainapptfg.R;

import java.util.ArrayList;

//https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
public class ListarYConectarBluetooth extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(device.getAddress());
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };


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


        this.listView = (ListView) findViewById(R.id.list);

        final ArrayList<String> list = new ArrayList<String>();

        this.adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });

        scanLeDevice(mBluetoothAdapter.isEnabled());

    }
    private boolean mScanning;


    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //mBluetoothAdapter.cancelDiscovery();
                    //mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //mBluetoothAdapter.startDiscovery();
        } else {
            mScanning = false;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

    }

}
