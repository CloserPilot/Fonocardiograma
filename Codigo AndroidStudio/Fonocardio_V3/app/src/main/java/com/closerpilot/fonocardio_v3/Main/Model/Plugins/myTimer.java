package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import java.util.Timer;
import java.util.TimerTask;

import com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__;
import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;

public class myTimer {
    private static final long TIMER_START = 0;
    private static final long TIMER_PERIOD = SEGUNDOS_1;
    private static final Timer timer = new Timer();
    private static TimerTask timerTask;
    private static boolean myTimmerRunning = false;


    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          PUBLIC           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Check if current Timer is Running
     * @return {@code True} if Timmer is running. {@code False} if otherwise.
     */
    public static boolean ismyTimmerRunning(){
        return myTimmerRunning;
    }


    /**
     * Starts the timer countdown
     */
    public static void startTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                myTimmerRunning = true;
                __PlugginControl__.toThreadDataHandler.sendEmptyMessage(HANDLER_BLUETOOTH_TIMER);
                __PlugginControl__.toMainHandler.sendEmptyMessage(HANDLER_CHRONOMETER_TIMER);
                __PlugginControl__.toMainHandler.sendEmptyMessage(HANDLER_ICON_BLINK_TIMER);
            }
        };

        timer.schedule(timerTask, TIMER_START, TIMER_PERIOD);
    }


    /**
     * Stops the timer
     */
    public static void stopTimer(){
        timerTask.cancel();
        myTimmerRunning = false;
    }


}
