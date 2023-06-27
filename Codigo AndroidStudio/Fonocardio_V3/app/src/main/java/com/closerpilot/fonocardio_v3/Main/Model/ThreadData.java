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

    //Bluetooth
    private static volatile String bluetoothMacAddress = null;
    private static volatile BluetoothDevice bluetoothDevice = null;
    private static volatile BluetoothSocket bluetoothSocket = null;
    private static final UUID BLUETOOTH_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");    // Service Identify - SPP UUID

    // I/O Buffers
    private static volatile InputStream bluetoothInputStream = null;
    private static volatile OutputStream bluetoothOutputStream = null;

    //Arrays to read a byte from bleutooth input
    private static final byte[] byteHigh = new byte[1];
    private static final byte[] byteLow = new byte[1];

    // Storage the data for a definec time set in myTimmer
    private static final Vector<Short> bufferDinamic = new Vector<>();

    // Boolean to know if current thread is running
    private static boolean threadDataRunning = false;



    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         PRIVADOS          //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Creates the bluetooth socket
     * @param device
     * @return The bluetooth socket, null if otherwise.
     * @throws IOException
     */
    private static BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (ActivityCompat.checkSelfPermission(__PlugginControl__.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return device.createRfcommSocketToServiceRecord(BLUETOOTH_MODULE_UUID);
        }
        return null;
    }


    /**
     * Links the I/O bluetooth buffers
     * @param socket The socket to link
     */
    private static void linkBuffersBluetooth(BluetoothSocket socket) {
        try {
            bluetoothInputStream = socket.getInputStream();
            bluetoothOutputStream = socket.getOutputStream();
            Log.d(TAG, "Conecta_buffers: Buffers conectados");
        } catch (IOException e) {
            Log.d(TAG, "Conecta_buffers: Error al conectar los buffers" + e);
        }
    }


    /**
     * Sends a String to the bluetooth output
     * @param str The string to send
     */
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


    /**
     * Sends an error to the MainActivity
     */
    private static void errorToMain_Bluetooth() {
        Message msg = Message.obtain();
        msg.what = HANDLER_ERROR;
        msg.obj = "Error en la conexión Bluetooth";
        __PlugginControl__.toMainHandler.sendMessage(msg);
    }


    /**
     * Connects the bluetooth to the given macAddress
     * @param macAddress The MAC-Address to connect the bluetooth
     */
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


    /**
     * Resets the bluetooth
     */
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

    /**
     * Checks if all the bluetooth parameters are set
     * @return {@code True} if all is set. {@code False} if otherwise.
     */
    public static boolean isBluetoothSocketReady() {
        return bluetoothMacAddress != null && bluetoothSocket != null && bluetoothInputStream != null && bluetoothOutputStream != null;
    }

    /**
     * Returns the state of the current Thread
     * @return {@code True} if Thread is running. {@code False} if otherwise.
     */
    public static boolean isThreadDataRunning(){
        return threadDataRunning;
    }

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         HANDLER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Handler for Thread Data
     */
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
                    if (isBluetoothSocketReady() && bufferDinamic.size() > 0 && startStopAttiny==ATTINY_START) {
                        try {
                            __PlugginControl__.bufferRawData.put(bufferDinamic.toArray(new Short[0]));
                            Log.d(TAG, "handleMessage: " + bufferDinamic.size());
                            Log.d(TAG, "Data send!!");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    bufferDinamic.clear();
                    break;
            }
        }
    };


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          LOPPER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Run method
     * If there is any data in the bluetooth buffer, storage it
     */
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

                    if (dataReceived < 1024) {              //If the data is more than 1024 error
                        if(INVERT_DATA) {                   //If the data is inverted due to the circuit
                            dataReceived = (short)(1024 - dataReceived);
                        }

                        bufferDinamic.add(dataReceived);
                    } else {
                        Log.d(TAG, "Datos Error");
                        bluetoothInputStream.read();    //Try to fix the error reading the next byte
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


