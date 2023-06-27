package com.closerpilot.fonocardio_v3.Main;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
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

    //Bluetooth indicators
    private boolean bluetoothBlink = false;
    private boolean bluetoothStatus = BLUETOOTH_DISCONNECTED;

    //UI
    private ImageView bluetoothBlinkImage;
    private TextView bluetoothStatusText;
    private Button buttonStart;
    private TextView errorText;
    private TextView fileName;
    private TextView baudRate;

    //Chronometer states (RUN-STAND_BY-STOP)
    private int chronometerRunning = STOP;

    //File name
    private String fileNameStr = "";

    //Pop up "configuration" window
    private RelativeLayout  relativeLayout;


    /**
     * Calls when Activity starts
     * @param savedInstanceState
     */
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
        baudRate = findViewById(R.id.id_baudRate);
        myPlotter.setPlotter(findViewById(R.id.id_plotter));
        myPlotter.setPlotterAuxLayout(findViewById(R.id.constraintLayoutPlot));
        myPlotter.setPlotterConfiguration();

        myChronometer.chronometer = findViewById(R.id.id_chronometer);
        __PlugginControl__.toThreadDataProcessHandler.sendEmptyMessage(HANDLER_BUFFERS_PATH);

        if (savedInstanceState != null) {
            fileNameStr = savedInstanceState.getString("fileNameStr");
            buttonStart.setText(savedInstanceState.getString("buttonState"));
            if (savedInstanceState.getInt("isTiming") == STAND_BY) {
                myChronometer.setBaseTime(savedInstanceState.getLong("time"));
                myChronometer.chronometer.start();
                chronometerRunning = savedInstanceState.getInt("isTiming");
            }
        }
    }

    /**
     * Calls when Activity is destroyed
     */
    @Override
    protected void onDestroy() {
        bluetoothBlinkImage = null;
        bluetoothStatusText = null;
        buttonStart = null;
        errorText = null;
        fileName = null;
        super.onDestroy();
    }


    /**
     * Starts the ThreadData thread
     */
    private void starThreadData() {
        if (!ThreadData.isThreadDataRunning())
            __PlugginControl__.threadData.start();
    }


    /**
     * Starts the ThreadDataProcess thread
     */
    private void startThreadDataProcess() {
        if (!ThreadDataProcess.isThreadDataProcessRunning())
            __PlugginControl__.threadDataProcess.start();
    }


    /**
     * Starts the ThreadHtppServer thread
     */
    private void startThreadHtppServer(){
        if(!ThreadHttpServer.isThreadHttpServerRunning())
            __PlugginControl__.threadHttpServer.start();
    }


    /**
     * Starts the timer
     */
    private void startmyTimmer(){
        if(!myTimer.ismyTimmerRunning())
            myTimer.startTimer();
    }


    /**
     * Save the important info before killing the Activity
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("time", myChronometer.chronometer.getBase());
        outState.putInt("isTiming", chronometerRunning);
        outState.putString("fileNameStr",fileNameStr);
        outState.putString("buttonState", buttonStart.getText().toString());
        super.onSaveInstanceState(outState);
    }


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         HANDLER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Handler for Main Activity
     */
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
                        baudRate.setText("BaudRate: " + msg.obj.toString());
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

    /**
     * OnClick method for the MainButton.
     * According to the state of the fileName and the Chronometer, take an action
     * @param view
     */
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

    /**
     * Start the sampling
     * @param fileName Name of the store file
     */
    private void buttonMain_Start(String fileName){
        buttonStart.setText(R.string.Stop);
        chronometerRunning = RUN;
        __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_START);
        errorText.setText("");
        myPlotter.resetBaudRate();

        Message msg = Message.obtain();
        msg.what = HANDLER_BUFFERS_LINK;
        msg.obj = fileName;
        __PlugginControl__.toThreadDataProcessHandler.sendMessage(msg);

        myToast.message("Iniciando...");
    }


    /**
     * Stop the sampling
     */
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


    /**
     * Deploys the menu
      * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.overflow, menu);   //R->carperta Res | menu->carpeta menu | overflow -> nombre de la activity
        return true;
    }


    /**
     * Execute an option depending of the item selected
      * @param item Selection
     * @return
     */
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

    /**
     * Pop up the configuration layout menu
     */
    private void configurationWindow() {
        // Inflate the layout of the popup window
        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.configuration, null);

        // Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
    }


    /**
     * Set the number of seconds to show in the plot
     * Set if ploter show
     * @param view
     */
    public void setConfiguration(View view){
        TextView bufferCountText = popupView.findViewById(R.id.id_bufferCount);
        Switch setPloter = popupView.findViewById(R.id.id_addplot);

        if(bufferCountText.getText().length()!=0) {
            NUM_OF_BUFFER_DISPLAYS = Integer.parseInt(bufferCountText.getText().toString());
        }

        if(setPloter.isChecked())
            PLOT = true;
        else {
            PLOT = false;
            myPlotter.cleanPlotter();
        }

        popupWindow.dismiss();
    }

}