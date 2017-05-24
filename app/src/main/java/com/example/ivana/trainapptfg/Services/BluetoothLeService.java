package com.example.ivana.trainapptfg.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.ivana.trainapptfg.Activities.Bluetooth.ListarYConectarBluetooth;

import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothLeService extends Service {

    //Colas para transmision de informacion desde el service
    private BlockingQueue<String> queueDispositivosEncontrados;
    private ListarYConectarBluetooth.MyResultReceiverBluetooth resultReceiver;

    // Accelerometer ranges
    private static final int ACC_RANGE_2G = 0;
    private static final int ACC_RANGE_4G = 1;
    private static final int ACC_RANGE_8G = 2;
    private static final int ACC_RANGE_16G = 3;

    //UUID Pulsera Acelerometro & Giroscopio CC2650 Sensor Tag
    private static final UUID UUID_MOVEMENT_SERVICE = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    private static final UUID UUID_MOVEMENT_DATA =    UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    private static final UUID UUID_MOVEMENT_CONF =    UUID.fromString("f000aa82-0451-4000-b000-000000000000");
    private static final UUID UUID_MOVEMENT_PERIOD =  UUID.fromString("f000aa83-0451-4000-b000-000000000000");
    //UUID Pulsera CCC
    private static final UUID UUID_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //ENCENDER/APAGAR SENSOR ACELERÓMETRO & GIROSCOPIO (MOVEMENT) CON UNA CONFIGURACION ESPECIFICA
    private static final byte[] ENCENDER_SENSOR_ACELEROMETRO = {0x3f, 0x02}; //array de bytes, bytes[0] es el byte menos significativo
    private static final byte[] APAGAR_SENSOR_ACELEROMETRO = {0x00, 0x00};
    private static final byte[] PERIODO_MOVEMENT_SENSOR = {0x0A};
    private static final int ACC_RANGE_ACTUAL_CONF = 0x03;
    //CONFIGURACION
    //5432 1098 7654 3210
    //XXXX XXXX XXXX XXXX
    //0000 0010 0011 1111         -> 0x003F -> 0x02, 0x3F        ^    MENOS SIGNF  [0x3F, 0x02] MAS SIGNF
    //0 gyro z                                                   |
    //1 gyro y                                                   |
    //2 gyro x
    //3 accel z
    //4 accel y
    //5 accel x
    //6 magnometer
    //7 wake on motion
    //8 accel range (2G 4G 8G 16G)  -> 8G : bit 9 a 1 y bit 8 a 0
    //9 accel range (2G 4G 8G 16G)
    //10 no se usa
    //11 no se usa
    //12 no se usa
    //13 no se usa
    //14 no se usa
    //15 no se usa

    //COLAS DE ESCRITURA PARA SENSOR
    private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
    private static boolean sIsWritting = false;

    //OTRAS CONSTANTES
    private static final double GRAVITIY = 9.81;

    //VARIABLES DE CONTROL
    private boolean bluetoothConfigurado = false;

    //CARACTERISTICAS/SERVICE/CONFIG DISPOSITIVO BLUETOOTH
    private BluetoothGattService acelerometroService;
    private BluetoothGattCharacteristic movementCharacteristic;
    private BluetoothGattCharacteristic movementConf;
    private BluetoothGattCharacteristic movementPeriod;
    private BluetoothGattDescriptor config;

    //CLASES BLUETOOTH
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private HashMap<String, BluetoothDevice> listaDevices;
    private IntentFilter filter;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra("EXTRA_STATUS", BluetoothGatt.GATT_SUCCESS);
        }
    };

    //Atiende a los eventos del bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("BLUETOOTH", "DISCOVERY STARTED"); //lo que se ejecuta cuando el bluetooth comienza a escanear
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //lo que se ejecuta cuando el bluetooth finaliza el escaneo
                Log.d("BLUETOOTH", "DISCOVERY FINISHED");

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { //lo que se ejecuta cuando el bluetooth encuentra un dispositivo
                final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BLUETOOTH", "DEVICE DISCOVERED");

                listaDevices.put(device.getAddress(), device);
                Bundle bundle = new Bundle();
                bundle.putString("address", device.getAddress());
                //TODO crear constantes para los codigos de envio
                resultReceiver.send(100, bundle);
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.add(device.getAddress());
                        listaDevices.put(device.getAddress(), device);
                        adapter.notifyDataSetChanged();
                    }
                });*/
            }
        }
    };

    private BluetoothGattCallback mBtleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("BLUETOOTH", "Estado de conexión bluetooth: " + (newState == BluetoothProfile.STATE_CONNECTED ? "Connected" : "Disconnected"));

            if(newState == BluetoothProfile.STATE_CONNECTED){
                //setState(State.CONNECTED);
                mBluetoothGatt.discoverServices();
            }
            else{
                //setState(State.IDDLE);
                //TODO Realizar todas las tareas necesarias cuando se desconecte la pulsera
                Log.d("BLUETOOTH", "Se ha desconectado el dispostivo bluetooth");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d("BLUETOOTH", "Servicios descubiertos :)");
                obtenerCaracteristicasDescriptoresAccelGyroCC2650(mBluetoothGatt);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("BLUETOOTH", "onCharacteristicWrite: " + status);
            sIsWritting = false;
            nextWrite(); //Si la cola de escriuras y no se está escribiendo -> escribeme
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d("BLUETOOTH", "onDescriptorWrite: " + status);
            sIsWritting = false;
            nextWrite(); //Si la cola de escriuras y no se está escribiendo -> escribeme
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("BLUETOOTH", "onCharacteristicChanged");

            if(characteristic.getUuid().equals(UUID_MOVEMENT_DATA)){
                byte[] valores = characteristic.getValue();

                double acc_x = (valores[7] << 8) +  valores[6];
                double acc_y = (valores[9] << 8) +  valores[8];
                double acc_z = (valores[11] << 8) +  valores[10];


                //TODO REVISAR DIRECCIONES (-1) Z(-1) X(-1) Y(1)
                double acc_scaledX = (sensorMpu9250AccConvert(acc_x) * GRAVITIY) * (-1);
                double acc_scaledY =  sensorMpu9250AccConvert(acc_y) * GRAVITIY;
                double acc_scaledZ = (sensorMpu9250AccConvert(acc_z) * GRAVITIY) * (-1);

                Log.d("ACELEROMETRO", "Value: " + acc_scaledX + " : " + acc_scaledY + " : " + acc_scaledZ);

                double gyro_x = (valores[1] << 8) +  valores[0];
                double gyro_y = (valores[3] << 8) +  valores[2];
                double gyro_z = (valores[5] << 8) +  valores[4];


                //TODO CONVERTIR A RADIANES/SEGUNDO (ESTTAN EN GRAD/S)
                double gyro_scaledX = (sensorMpu9250GyroConvert(gyro_x)) * (Math.PI/180) * (-1);
                double gyro_scaledY = (sensorMpu9250GyroConvert(gyro_y)) * (Math.PI/180);
                double gyro_scaledZ = (sensorMpu9250GyroConvert(gyro_z)) * (Math.PI/180) * (-1);

                Log.d("GIROSCOPIO  ", "Value: " + gyro_scaledX + " : " + gyro_scaledY + " : " + gyro_scaledZ);

            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        this.listaDevices = new HashMap<String, BluetoothDevice>();
        this.queueDispositivosEncontrados = null;
    }

    @Override
    public void onDestroy(){
        mBluetoothAdapter.cancelDiscovery();
        if(bluetoothConfigurado){
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    public void inicializarColaDispositivosEncontrados(BlockingQueue<String> queueDispositivosEncontrados){
        this.queueDispositivosEncontrados = queueDispositivosEncontrados;
    }

    public void inicializarResultReceiver(ListarYConectarBluetooth.MyResultReceiverBluetooth rr){
        this.resultReceiver = rr;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //Para poder ejecutar metodos del service desde una activity////////////////////////////////
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder{
        public BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos publicos accesibles de este servicio///////////////////////////////////////////////
    public boolean mensaje_configurarBluetooth(){
        //boolean bluetoothDesactivado = false;
        boolean bluetoothActivado = true;

        //OBRTENEMOS EL ADAPTADOR DEL BLUETOOTH
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        //COMPROBAMOS QUE ESTÉ ACTIVADO EL BLUETOOTH
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);*/
            bluetoothActivado = false;
        }

        //TODO poner variable de bluetoothDesacitivado global para que en onDestroy no falle si no se ha registrado receiver
        if(bluetoothActivado){
            //REGISTRAR BROADCAST BLUETOOTH
            registerReceiver(mReceiver, filter);
        }

        this.bluetoothConfigurado = bluetoothActivado;
        return bluetoothActivado;

    }

    public boolean mensaje_conectarDispositivo(String mac){
        boolean conectado = false;

        BluetoothDevice device = this.listaDevices.get(mac);
        if(device != null){
            this.mBluetoothGatt = device.connectGatt(this, true, this.mBtleCallback);
            conectado = true;
        }

        return conectado;
    }

    public void mensaje_startDiscovery(){
        this.listaDevices.clear();
        this.mBluetoothAdapter.startDiscovery();
    }

    public void mensaje_encenderSensorCC2650(){
        if(this.config != null){
            mBluetoothGatt.setCharacteristicNotification(movementCharacteristic, true);

            movementConf.setValue(ENCENDER_SENSOR_ACELEROMETRO);
            write(movementConf);

            this.config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            write(config);

            this.movementPeriod.setValue(PERIODO_MOVEMENT_SENSOR);
            write(movementPeriod);
        }
    }

    public void mensaje_apagarSensorCC2650(){
        if(this.config != null) {
            movementConf.setValue(APAGAR_SENSOR_ACELEROMETRO);
            write(movementConf);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos para convertir los datos obtenidos por la pulsera CC2650///////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    private double sensorMpu9250GyroConvert(double data){
        //-- calculate rotation, unit deg/s, range -250, +250
        return (data * 1.0) / (65536 / 500);
    }

    private double sensorMpu9250AccConvert(double rawData) {
        double v = -1;

        int accRange = ENCENDER_SENSOR_ACELEROMETRO[1] & ACC_RANGE_ACTUAL_CONF;
        switch (accRange)
        {
            case ACC_RANGE_2G:
                //-- calculate acceleration, unit G, range -2, +2
                v = (rawData * 1.0) / (32768/2);
                break;

            case ACC_RANGE_4G:
                //-- calculate acceleration, unit G, range -4, +4
                v = (rawData * 1.0) / (32768/4);
                break;

            case ACC_RANGE_8G:
                //-- calculate acceleration, unit G, range -8, +8
                v = (rawData * 1.0) / (32768/8);
                break;

            case ACC_RANGE_16G:
                //-- calculate acceleration, unit G, range -16, +16
                v = (rawData * 1.0) / (32768/16);
                break;
        }

        return v;
    }

    private void obtenerCaracteristicasDescriptoresAccelGyroCC2650(BluetoothGatt gatt){
        BluetoothGattService acelerometroService = gatt.getService(UUID_MOVEMENT_SERVICE);
        if(acelerometroService != null){
            this.movementCharacteristic = acelerometroService.getCharacteristic(UUID_MOVEMENT_DATA);
            this.movementConf = acelerometroService.getCharacteristic(UUID_MOVEMENT_CONF);
            this.movementPeriod = acelerometroService.getCharacteristic(UUID_MOVEMENT_PERIOD);

            if(movementCharacteristic != null && movementConf != null){
                this.config = movementCharacteristic.getDescriptor(UUID_CCC);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos para escribir sobre caracteristicas bluetooth//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    //Si no hay nadie escribiendo y la lista no está vacia, que escriba el siguiente que tiene de escribir
    private synchronized void nextWrite(){
        if(!sIsWritting && !sWriteQueue.isEmpty()){
            doWrite(sWriteQueue.poll()); //escribirá el primero de la lista
        }
    }

    //Realiza el proceso de escritura diferenciando entre descriptor/característica
    private synchronized void doWrite(Object o){
        if(o instanceof BluetoothGattCharacteristic){
            sIsWritting = true;
            mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic)o);
        }
        else if(o instanceof BluetoothGattDescriptor){
            sIsWritting = true;
            mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor)o);
        }
        else{
            nextWrite();
        }
    }

    private synchronized void write(Object o){
        if(sWriteQueue.isEmpty() && !sIsWritting){
            doWrite(o);
        }else{
            sWriteQueue.add(o);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
}
