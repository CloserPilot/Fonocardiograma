package com.closerpilot.fonocardio_v3.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.closerpilot.fonocardio_v3.Main.Model.ThreadDataProcess;
import com.closerpilot.fonocardio_v3.Main.Model.ThreadHttpServer;
import com.closerpilot.fonocardio_v3.Main.Model.__Constants__;
import com.closerpilot.fonocardio_v3.R;
import com.closerpilot.fonocardio_v3.Main.Model.ThreadData;
import com.closerpilot.fonocardio_v3.LinkedDevices.LinkedDevicesActivity;
import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;

import com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__;
import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myChronometer;
import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myPlotter;
import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myTimer;
import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myToast;
import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myVibrator;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    //Indicador al usuario del estado Bluetooth
    private boolean bluetoothBlink = false;
    private boolean bluetoothStatus = BLUETOOTH_DISCONNECTED;

    //Interfaz gráfica
    private ImageView bluetoothBlinkImage;
    private TextView bluetoothStatusText;
    private Button buttonStart;
    private TextView errorText;
    private TextView fileName;

    //Variable para ocultar/mostrat el Path
    private boolean showPath = false;

    //Variable de estado del cronómetro (RUN-STAND_BY-STOP)
    private int chronometerRunning = STOP;

    //Variable para el nombre del archivo
    private String fileNameStr = "";

    private RelativeLayout  relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayout = findViewById(R.id.id_relative);
        System.gc();

        __PlugginControl__.context = MainActivity.this;
        __PlugginControl__.toMainHandler = mainHandler;

        starThreadData();
        startThreadDataProcess();
        startThreadHtppServer();
        startmyTimmer();

        bluetoothBlinkImage = findViewById(R.id.id_status_bluetooth_icon);
        bluetoothStatusText = findViewById(R.id.id_status_bluetooth);
        buttonStart = findViewById(R.id.id_button_main);
        errorText = findViewById(R.id.id_error);
        errorText.setText("");
        fileName = findViewById(R.id.id_file_name);
        myPlotter.setPlotter(findViewById(R.id.id_plotter));
        myPlotter.setPlotterAuxLayout(findViewById(R.id.constraintLayoutPlot));
        myPlotter.setPlotterConfiguration();

        myChronometer.cronometro = findViewById(R.id.id_chronometer);
        __PlugginControl__.toThreadDataProcessHandler.sendEmptyMessage(HANDLER_BUFFERS_PATH);

        if (savedInstanceState != null) {
            fileNameStr = savedInstanceState.getString("fileNameStr");
            buttonStart.setText(savedInstanceState.getString("buttonState"));
            if (savedInstanceState.getInt("isTiming") == STAND_BY) {
                myChronometer.setBaseTime(savedInstanceState.getLong("time"));
                myChronometer.cronometro.start();
                chronometerRunning = savedInstanceState.getInt("isTiming");
            }
        }
    }


    private void starThreadData() {
        if (!ThreadData.isThreadDataRunning())
            __PlugginControl__.threadData.start();
    }

    private void startThreadDataProcess() {
        if (!ThreadDataProcess.isThreadDataProcessRunning())
            __PlugginControl__.threadDataProcess.start();
    }

    private void startThreadHtppServer(){
        if(!ThreadHttpServer.isThreadHttpServerRunning())
            __PlugginControl__.threadHttpServer.start();
    }

    private void startmyTimmer(){
        if(!myTimer.ismyTimmerRunning())
            myTimer.startTimer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("time", myChronometer.cronometro.getBase());
        outState.putInt("isTiming", chronometerRunning);
        outState.putString("fileNameStr",fileNameStr);
        outState.putString("buttonState", buttonStart.getText().toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        //myTimer.stopTimer();
        //myChronometer.stopChronometer();

        //__PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_RESET);
        //__PlugginControl__.toThreadDataProcessHandler.sendEmptyMessage(HANDLER_BUFFERS_CLEAR);
        //__PlugginControl__.context = null;
        //__PlugginControl__.toMainHandler = null;

        //Reset de variables
        //bluetoothBlinkImage = null;
        //bluetoothStatusText = null;
        //buttonStart = null;
        //errorText = null;
        //fileName = null;

        //myPlotter.cleanPlotter();
        //myPlotter.setPlotter(null);
        //myPlotter.setPlotterAuxLayout(null);

        //myChronometer.cronometro = null;
        //mainHandler = null;
        super.onDestroy();
    }


    private Handler mainHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                switch (msg.what) {
                    case HANDLER_ICON_BLINK_TIMER:
                        if (bluetoothStatus) {
                            bluetoothStatusText.setText((R.string.id_bluetooth_connected));
                            bluetoothBlinkImage.setImageResource(android.R.drawable.presence_online);
                        } else {
                            bluetoothStatusText.setText((R.string.id_bluetooth_disconnected));
                            bluetoothBlinkImage.setImageResource(android.R.drawable.presence_busy);
                        }

                        if (bluetoothBlink) {
                            bluetoothBlinkImage.setVisibility(View.VISIBLE);
                        } else {
                            bluetoothBlinkImage.setVisibility(View.INVISIBLE);
                        }
                        bluetoothBlink = !bluetoothBlink;

                        if (bluetoothStatus != ThreadData.isBluetoothSocketReady())
                            myVibrator.vibrate(100);
                        bluetoothStatus = ThreadData.isBluetoothSocketReady();

                        break;

                    case HANDLER_CHRONOMETER_TIMER:
                        if (chronometerRunning == RUN) {
                            myChronometer.startChronometer();
                            chronometerRunning = STAND_BY;
                        } else if (chronometerRunning == STOP)
                            myChronometer.stopChronometer();
                        break;

                    case HANDLER_BUFFERS_BAUDRATE:
                        //myPlotter.addEntry();
                        break;

                    case HANDLER_ERROR:
                        buttonMain_Stop();
                        myToast.message((String) msg.obj);
                        errorText.setText((String) msg.obj);
                        myVibrator.vibrate(50);
                        break;

                    case HANDLER_DEBUG:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////           BUTTON          //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////
    public void buttonMain(View view){
        fileNameStr = fileName.getText().toString();

        if(ThreadData.isBluetoothSocketReady()) {
            if (fileNameStr.length() != 0) {
                if (chronometerRunning == STOP)
                    buttonMain_Start(fileNameStr);
                else if (chronometerRunning == STAND_BY)
                    buttonMain_Stop();
            }
            else
                myToast.message("Ingrese un nombre al archivo");
        }
        else
            myToast.message("Conecte el Bluetooth primero");

        myVibrator.vibrate(50);
    }

    private void buttonMain_Start(String fileName){
        buttonStart.setText(R.string.Stop);
        chronometerRunning = RUN;
        __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_START);
        errorText.setText("");

        Message msg = Message.obtain();
        msg.what = HANDLER_BUFFERS_LINK;
        msg.obj = fileName;
        __PlugginControl__.toThreadDataProcessHandler.sendMessage(msg);

        myToast.message("Iniciando...");
    }

    private void buttonMain_Stop(){
        buttonStart.setText(R.string.Reset);
        chronometerRunning = STOP;
        __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_STOP);
        __PlugginControl__.toThreadDataProcessHandler.sendEmptyMessage(HANDLER_BUFFERS_FLUSH);
        myToast.message("Deteniendo");
        System.gc();
    }


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////        ACTION_BAR         //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////


    //Despliega el menu
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.overflow, menu);   //R->carperta Res | menu->carpeta menu | overflow -> nombre de la activity
        return true;
    }


    //Ejecuta una accion segun el icono seleccionado
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();      //Recupera el item seleccionado

        if(id == R.id.id_info)
            myToast.message("Fonocardiograma V3\nCloser P.\nMario A.P.H. ♥");
        else if(id == R.id.id_settings){
            configurationWindow();
        }else
            myVibrator.vibrate(50);

        if(id == R.id.id_bluetooth){
            __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_RESET);    //Limpia los parametros del Bluetooth para evitar basura
            startActivity(new Intent(this, LinkedDevicesActivity.class));           //Intent para la ventana de dispositivos vinculados
        }

        if(id == R.id.id_path)
            errorText.setText("Ubicacion para guardar archivos   "+__PlugginControl__.threadDataProcess.getPath());


        if(id == R.id.id_ip_address)
            errorText.setText("Dirección IP " + __PlugginControl__.threadHttpServer.getIpAddress());

        return super.onOptionsItemSelected(item);
    }


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////    CONFIGURATION MENU     //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////
    private PopupWindow popupWindow = null;
    private LayoutInflater inflater = null;
    private View popupView = null;

    private void configurationWindow() {
        // inflate the layout of the popup window
        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.configuration, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);

        /*
        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //popupWindow.dismiss();
                return true;
            }
        });
        */

    }


    public void setBufferCount(View view){
        TextView bufferCountText = popupView.findViewById(R.id.id_bufferCount);

        if(bufferCountText.getText().length()!=0) {
            NUM_OF_BUFFER_DISPLAYS = Integer.parseInt(bufferCountText.getText().toString());
        }
        popupWindow.dismiss();
    }

}