package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.widget.Toast;

import static com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__.*;

public class myToast {
    private static Toast toast;

    /**
     * Send a Toast to the Main UI
     * @param msg the message to display on the Main UI
     */
    public static void message(String msg){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        toast.show();
        myVibrator.vibrate(50);
    }
}
