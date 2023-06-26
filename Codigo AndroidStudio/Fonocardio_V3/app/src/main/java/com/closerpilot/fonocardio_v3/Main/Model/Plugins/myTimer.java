package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;
import com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__;

import java.util.Timer;
import java.util.TimerTask;

public class myTimer {
    private static final long TIMER_START = 0;
    private static final long TIMER_PERIOD = SEGUNDOS_1;
    private static final Timer timer = new Timer();
    private static TimerTask timerTask;

    //private static final int[] contador = {0};
    private static boolean myTimmerRunning = false;

    public static boolean ismyTimmerRunning(){
        return myTimmerRunning;
    }


    public static void startTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                myTimmerRunning = true;
                __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_TIMER);
                __PlugginControl__.toMainHandler.sendEmptyMessage(HANDLER_CHRONOMETER_TIMER);
                __PlugginControl__.toMainHandler.sendEmptyMessage(HANDLER_ICON_BLINK_TIMER);

                //if(__PlugginControl__.webSocketHttp != null)
                //    __PlugginControl__.webSocketHttp.broadcast("Egg :" + contador[0]++);
            }
        };

        timer.schedule(timerTask, TIMER_START, TIMER_PERIOD);
    }

    public static void stopTimer(){
        timerTask.cancel();
        myTimmerRunning = false;
    }


}
