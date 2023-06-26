package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.widget.Chronometer;

public class myChronometer {
    private static boolean running = false;

    @SuppressLint("StaticFieldLeak")
    public static Chronometer cronometro;

    public static void startChronometer(){
        cronometro.setBase(SystemClock.elapsedRealtime());
        cronometro.start();
    }

    public static void stopChronometer(){
        cronometro.stop();
    }

    public static void setBaseTime(long time){
        cronometro.setBase(time);
    }



}
