package com.closerpilot.fonocardio_v3.Main.Model;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;

import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myToast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;


public class ThreadData extends Thread {
    private static final String TAG = "ThreadData";

    //Variables Bluetooth
    private static volatile String bluetoothMacAddress = null;
    private static volatile BluetoothDevice bluetoothDevice = null;
    private static volatile BluetoothSocket bluetoothSocket = null;
    private static final UUID BLUETOOTH_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");    // Identificador unico de servicio - SPP UUID

    //Buffers de entrada/salida
    private static volatile InputStream bluetoothInputStream = null;
    private static volatile OutputStream bluetoothOutputStream = null;

    //Arreglo de un byte para leer del buffer bluetooth
    private static final byte[] byteHigh = new byte[1];
    private static final byte[] byteLow = new byte[1];

    //Estructura para almacenar los datos por el tiempo definido en myTimer
    private static final Vector<Short> bufferDinamic = new Vector<>();

    //Variable para saber si el hilo ya esta corriendo
    private static boolean threadDataRunning = false;



    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         PRIVADOS          //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////
    //Crea el socketBluetooth
    private static BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (ActivityCompat.checkSelfPermission(__PlugginControl__.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return device.createRfcommSocketToServiceRecord(BLUETOOTH_MODULE_UUID);
        }
        return null;
    }


    //Liga los buffers de entrada y salida
    private static void linkBuffersBluetooth(BluetoothSocket socket) {
        try {
            bluetoothInputStream = socket.getInputStream();
            bluetoothOutputStream = socket.getOutputStream();
            Log.d(TAG, "Conecta_buffers: Buffers conectados");
        } catch (IOException e) {
            Log.d(TAG, "Conecta_buffers: Error al conectar los buffers" + e);
        }
    }


    //Manda una secuencia de caracteres
    private static void writeBluetooth(@NonNull String str) {
        if (isBluetoothSocketReady()) {
            try {
                bluetoothOutputStream.write(str.getBytes());
            } catch (IOException e) {
                Log.d(TAG, "Write fallo :" + e);
                myToast.message("Error de conexión bluetooth");
                resetBluetooth();
                errorToMain_Bluetooth();
            }
        }
    }


    //Manda un error a la UIThread
    private static void errorToMain_Bluetooth() {
        Message msg = Message.obtain();
        msg.what = HANDLER_ERROR;
        msg.obj = "Error en la conexión Bluetooth";
        __PlugginControl__.toMainHandler.sendMessage(msg);
    }


    //Crea las conexiones Bluetooth
    private static void connectBluetooth(String macAddress) {
        resetBluetooth();
        bluetoothMacAddress = macAddress;

        if (bluetoothMacAddress != null) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothMacAddress);

            try {
                bluetoothSocket = createBluetoothSocket(bluetoothDevice);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "conecta_bt: La creacción del Socket fallo");
            }

            try {
                if (ActivityCompat.checkSelfPermission(__PlugginControl__.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    bluetoothSocket.connect();
                    linkBuffersBluetooth(bluetoothSocket);
                }
            } catch (IOException e) {
                Log.d(TAG, "conecta_bt: error " + e);
                resetBluetooth();
            }
        } else {
            Log.d(TAG, "MAC: " + macAddress);
        }
    }


    //Reinicia los parametros del bluetooth
    private static void resetBluetooth() {

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bluetoothInputStream != null) {
            try {
                bluetoothInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bluetoothOutputStream != null) {
            try {
                bluetoothOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bluetoothDevice = null;
        bluetoothMacAddress = null;
        bluetoothSocket = null;
        bluetoothInputStream = null;
        bluetoothOutputStream = null;
    }

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         PUBLICOS          //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    //Verifica si todos los parámetros son validos
    public static boolean isBluetoothSocketReady() {
        return bluetoothMacAddress != null && bluetoothSocket != null && bluetoothInputStream != null && bluetoothOutputStream != null;
    }

    //Sabe si el hilo esta corriento
    public static boolean isThreadDataRunning(){
        return threadDataRunning;
    }

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         HANDLER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    private static char startStopAttiny = ATTINY_STOP;
    public static Handler threadDataHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case HANDLER_BLUETOOTH_RESET:
                    startStopAttiny = ATTINY_STOP;
                    resetBluetooth();
                    break;

                case HANDLER_BLUETOOTH_CONNECT:
                    connectBluetooth((String)msg.obj);
                    break;

                case HANDLER_BLUETOOTH_START:
                    startStopAttiny = ATTINY_START;
                    break;

                case HANDLER_BLUETOOTH_STOP:
                    startStopAttiny = ATTINY_STOP;
                    break;

                case HANDLER_BLUETOOTH_TIMER:
                    writeBluetooth(Character.toString(startStopAttiny));
                    if (isBluetoothSocketReady() && bufferDinamic.size() > 0) {
                        try {
                            __PlugginControl__.bufferRawData.put(bufferDinamic.toArray(new Short[0]));
                            Log.d(TAG, "handleMessage: " + bufferDinamic.size());
                            bufferDinamic.clear();
                            Log.d(TAG, "Data send!!");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          LOPPER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    @Override
    public void run() {
        __PlugginControl__.toThreadDataHandler = threadDataHandler;
        threadDataRunning = true;

        while (!ThreadData.currentThread().isInterrupted()) {
            if (isBluetoothSocketReady()) {
                try {
                    bluetoothInputStream.read(byteHigh);
                    bluetoothInputStream.read(byteLow);
                    short dataReceived = (short) (((byteHigh[0] & 0xff) << 8) | (byteLow[0] & 0xff));

                    if (dataReceived < 1024) {              //Al ser solo de 10 bits, el dato no puede ser mayor a 1024, caso contrario es un error
                        if(INVERT_DATA) {                   //If the data is inverted due to the circuit
                            dataReceived = (short)(1024 - dataReceived);
                        }

                        bufferDinamic.add(dataReceived);
                    } else {
                        Log.d(TAG, "Datos Error");
                        bluetoothInputStream.read();    //Se lee el siguiente byte para intentar remedear el error
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    errorToMain_Bluetooth();
                    resetBluetooth();
                }
            }
        }
    }
}


