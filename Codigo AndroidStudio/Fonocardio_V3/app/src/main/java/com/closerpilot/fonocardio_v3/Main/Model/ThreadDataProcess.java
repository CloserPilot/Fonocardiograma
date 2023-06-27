package com.closerpilot.fonocardio_v3.Main.Model;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;

import com.closerpilot.fonocardio_v3.Main.Model.Plugins.myPlotter;

import org.json.JSONArray;
import org.json.JSONException;

public class ThreadDataProcess extends Thread {
    private static final String TAG = "HILO_ARCHIVOS";

    //File
    private static File file = null;
    private static File filePath = null;
    private static FileWriter fileWriter = null;
    private static PrintWriter printwriter = null;
    private static String fileName = null;

    // Boolean to know if current thread is running
    private static boolean threadDataProcessRunning = false;


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          PRIVATE          //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Check if Storage is Writable and Readable.
     * @return {@code True} if Storage is Writable and Readable. {@code False} if otherwise.
     */
    private static boolean isExternalStorageWritableReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        errorToMain_Files("Error con los permisos de almanecamiento en SD");
        return false;
    }


    /**
     * Check and change the filePath in relation with MainActivity's storage path
     */
    private static void checkWriters() {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(__PlugginControl__.context, null);

        if (isExternalStorageWritableReadable()) {

            //Selecciona la ubicacion para los archivos
            if (externalStorageVolumes.length > 1)
                filePath = externalStorageVolumes[1];
            else
                filePath = externalStorageVolumes[0];
        } else
            errorToMain_Files("Error al buscar las rutas de escritura");
    }


    /**
     * Link the output buffer
     */
    private static void linkBuffer() {
        file = new File(filePath.getAbsolutePath(), (fileName + ".txt"));

        try {
            if(file.exists())
                file.delete();

            file.createNewFile();
            fileWriter = new FileWriter(file, false);
            printwriter = new PrintWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
            __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_RESET);
            errorToMain_Files("Error al ligar buffers de Escritura");
            destroyBuffers();
        }
    }


    /**
     * Unbind all information related with storage
     * */
    private static void destroyBuffers(){
        file = null;
        filePath = null;
        fileWriter = null;
        printwriter = null;
        fileName = null;
    }


    /**
     * Save the data to the file
     */
    private static void saveOnFile(Short[] data) {
        if (isExternalStorageWritableReadable()) {
            String dataToString = Arrays.toString(data).replaceAll("\\[|\\]|\\s", "");
            printwriter.print(dataToString.replaceAll(",", "\n") + "\n");
        }
    }


    /**
     * Save the data to the file without format
     */
    private static void saveOnFileNoFormat(String data) {
        if (isExternalStorageWritableReadable()) {
            printwriter.print(data);
        }
    }


    /**
     * Send a message to the MainActivity
     */
    private static void errorToMain_Files(String msgText) {
        Message msg = Message.obtain();
        msg.what = HANDLER_ERROR;
        msg.obj = msgText;
        __PlugginControl__.toMainHandler.sendMessage(msg);
    }


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          PUBLIC           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Write BaudRate and close the file.
     */
    public static void flushData(){
        if (printwriter != null) {
            int baudRate = myPlotter.getBaudRate();

            //Write the BaudRate at the end of the file
            if (baudRate>0)
                saveOnFileNoFormat("\n\n\n@@" + String.valueOf(baudRate));

            printwriter.flush();
            printwriter.close();
        }

        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the path of the file
     * @return {@code filePath}
     */
    public static String getPath() {
        return filePath.toString();
    }

    /**
     * Returns the state of the current Thread
     * @return {@code True} if Thread is running. {@code False} if otherwise.
     */
    public static boolean isThreadDataProcessRunning(){
        return threadDataProcessRunning;
    }


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////         HANDLER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////
    /**
     * Handler for Thread Data
     */
    public static Handler threadDataProcessHandler = new Handler(Looper.myLooper()) {
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case HANDLER_BUFFERS_PATH:
                    checkWriters();
                    break;

                case HANDLER_BUFFERS_LINK:
                    checkWriters();
                    fileName = ((String) msg.obj);
                    linkBuffer();
                    myPlotter.cleanPlotter();
                    break;

                case HANDLER_BUFFERS_FLUSH:
                    flushData();
                    break;

                case HANDLER_BUFFERS_CLEAR:
                    flushData();
                    destroyBuffers();
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
     * If there is information in the bufferRawData, save the data on the file and redirect to bufferData.
     */
    @Override
    public void run() {
        __PlugginControl__.toThreadDataProcessHandler = threadDataProcessHandler;
        threadDataProcessRunning = true;

        while (!ThreadDataProcess.currentThread().isInterrupted()) {
            try {
                Short[] buffer = __PlugginControl__.bufferRawData.take();
                //|-----------------------------------------------------------------------------------------------------------------|
                //<---------------------------      HERE Add some type of data processing       ------------------------------------>
                //|-----------------------------------------------------------------------------------------------------------------|
                __PlugginControl__.bufferData.put(buffer);                                          //Send the process buffer to the plotter

                if(__PlugginControl__.webSocketHttp!=null)
                    __PlugginControl__.webSocketHttp.broadcast(new JSONArray(buffer).toString());   //Send the process buffer to LAN

                //Plot the data
                myPlotter.addEntry();

                //Save on file
                if (fileName != null)
                    saveOnFile(buffer);

            } catch (InterruptedException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}