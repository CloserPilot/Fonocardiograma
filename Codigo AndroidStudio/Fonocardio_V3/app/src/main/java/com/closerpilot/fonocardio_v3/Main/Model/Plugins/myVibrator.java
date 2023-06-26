package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.content.Context;
import android.os.Vibrator;
import static com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__.*;

public class myVibrator {

    //Vibra por tiempo*ms
    public static void vibrate(int time) {
        Vibrator vibra = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibra.vibrate(time);
    }
}
