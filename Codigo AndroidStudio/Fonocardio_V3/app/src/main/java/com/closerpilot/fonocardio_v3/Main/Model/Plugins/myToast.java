package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.widget.Toast;
import static com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__.*;

public class myToast {

    //Manda un Toast hacia el mainActivity
    private static Toast toast;
    public static void message(String mensaje){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context,mensaje,Toast.LENGTH_SHORT);
        toast.show();
        myVibrator.vibrate(50);
    }
}
