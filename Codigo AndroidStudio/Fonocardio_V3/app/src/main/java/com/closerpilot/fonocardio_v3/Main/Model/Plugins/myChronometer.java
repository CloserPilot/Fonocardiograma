package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.os.SystemClock;
import android.widget.Chronometer;

public class myChronometer {
    private static boolean running = false;

    public static Chronometer cronometro;

    /**
     * Start the Chronometer count
     */
    public static void startChronometer(){
        cronometro.setBase(SystemClock.elapsedRealtime());
        cronometro.start();
    }

    /**
     * Stop the Chronometer count
     */
    public static void stopChronometer(){
        cronometro.stop();
    }

    /**
     * Set the new base time
     * @param time base time
     */
    public static void setBaseTime(long time){
        cronometro.setBase(time);
    }

}
