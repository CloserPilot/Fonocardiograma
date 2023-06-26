package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.os.SystemClock;
import android.widget.Chronometer;

public class myChronometer {
    public static Chronometer chronometer;

    /**
     * Start the Chronometer count
     */
    public static void startChronometer(){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    /**
     * Stop the Chronometer count
     */
    public static void stopChronometer(){
        chronometer.stop();
    }

    /**
     * Set the new base time
     * @param time base time
     */
    public static void setBaseTime(long time){
        chronometer.setBase(time);
    }

}
