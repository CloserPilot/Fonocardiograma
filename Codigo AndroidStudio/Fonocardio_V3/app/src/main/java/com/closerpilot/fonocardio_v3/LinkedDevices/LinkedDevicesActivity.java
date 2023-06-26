package com.closerpilot.fonocardio_v3.LinkedDevices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;

import com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__;
import com.closerpilot.fonocardio_v3.R;

import java.util.Set;


public class LinkedDevicesActivity extends AppCompatActivity {
    private static final String TAG = "LinkedDevices";         //Etiqueta para buscar el LOGCAT en la terminal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_devices);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBluetooth();
    }


    //Verifica el estado actual del Bluetooth, si se encuentra apagado manda una solicitud al usuario para encenderlo
    private void checkBluetooth() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null)
            Toast.makeText(getBaseContext(), "Error con el dispositivo Bluetooth", Toast.LENGTH_SHORT).show();
        else{
            if (!btAdapter.isEnabled() && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));      //Intenta activar el BT
            Log.d(TAG, "Estado del Bluetooth" + (btAdapter.isEnabled()));

            showDevices(btAdapter);                                                      //Ingresa los dispositivos vinculados en un array para que el usuario seleccione uno
        }
    }


    //Despliega la lista de dispositivos emparejados
    private void showDevices(BluetoothAdapter btAdapter){
        ArrayAdapter<String> pairedDevicesArray;
        ListView listContainer;

        pairedDevicesArray = new ArrayAdapter<>(this, R.layout.found_devices);
        listContainer = findViewById(R.id.PairedDevices);
        listContainer.setAdapter(pairedDevicesArray);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();              //Obtiene la lista de nombres de dispositivos BT

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices)
                    pairedDevicesArray.add(device.getName() + "\n" + device.getAddress());  //Enlista los dispositivos emparejados en el Array
            }
        }

        listContainer.setOnItemClickListener(mDeviceClickListener);                          //Espera a que el usuario seleccione un dispositivo
    }


    //Cuando el usuario selecciona un dispositivo de la lista, la información se manda a al ThreadDatos. Cierra la activity
    private final AdapterView.OnItemClickListener mDeviceClickListener = (av, v, arg2, arg3) -> {
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17);                // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista

        Log.d(TAG, "address: " + address);

        Message msg = Message.obtain();
        msg.what = HANDLER_BLUETOOTH_CONNECT;
        msg.obj = address;
        __PlugginControl__.toThreadDataHandler.sendMessage(msg);

        finish();
    };
}