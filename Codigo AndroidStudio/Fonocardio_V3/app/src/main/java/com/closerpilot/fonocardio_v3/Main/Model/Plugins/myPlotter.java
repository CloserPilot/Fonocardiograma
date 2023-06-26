package com.closerpilot.fonocardio_v3.Main.Model.Plugins;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.closerpilot.fonocardio_v3.Main.Model.__PlugginControl__;
import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;



public class myPlotter {
    private static final String TAG = "Plotter";

    //Variables del plot
    private static Viewport viewport;
    private static GraphView plotter;
    private static View plotterAuxLayout;

    //Manda el BaudRate al main thread
    private static void sendBaudRateToMain(int baudRate){
        Message msg = Message.obtain();
        msg.what = HANDLER_BUFFERS_BAUDRATE;
        msg.obj = baudRate;
        __PlugginControl__.toMainHandler.sendMessage(msg);
    }




    public static void setPlotter(GraphView plot){
        plotter = plot;
    }

    public static void setPlotterAuxLayout(View auxLayout){
        plotterAuxLayout = auxLayout;
    }

    public static void setPlotterConfiguration(){
        viewport = plotter.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(1200);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(1200);
        viewport.setScrollableY(false);
        viewport.setScrollable(false);

        if((__PlugginControl__.context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            viewport.setBackgroundColor(Color.DKGRAY);
        else
            viewport.setBackgroundColor(Color.GRAY);

        //Title
        plotter.setTitle("Fonocardio V3.0");
        plotter.setTitleTextSize(40);
        plotter.setTitleColor(Color.CYAN);


        //Grid
        plotter.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        plotter.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        plotter.getGridLabelRenderer().setTextSize(34);
        if((__PlugginControl__.context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            plotter.getGridLabelRenderer().setGridColor(Color.GRAY);
        else
            plotter.getGridLabelRenderer().setGridColor(Color.WHITE);
        plotter.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        plotter.getGridLabelRenderer().reloadStyles();


        //Plot Background color
        if((__PlugginControl__.context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            plotter.setBackgroundColor(Color.BLACK);
            plotterAuxLayout.setBackgroundColor(Color.BLACK);
        }
        else {
            plotter.setBackgroundColor(Color.DKGRAY);
            plotterAuxLayout.setBackgroundColor(Color.DKGRAY);
        }
    }

    public static void cleanPlotter(){
        myPlotter.plotter.removeAllSeries();
        packagesReceive = 0;
        dataCount = 0;
    }

    public static int getBaudRate(){
        return baudRate;
    }


    //<----------------------------------------------------------------------------------->
    //<----------------------------------------------------------------------------------->
    //<--------------    CREA MUCHA BASURA HAY QUE MODIFICAR EL CODIGO     --------------->
    //<----------------------------------------------------------------------------------->
    //<----------------------------------------------------------------------------------->
    private static int dataCount = 0;
    private static int packagesReceive = 0;
    private static volatile int baudRate = 0;

    public static void addEntry(){
        try {
            System.gc();
            Short[] buffer = __PlugginControl__.bufferData.take();

            DataPoint[] dataPoints = new DataPoint[buffer.length];

            for (int i = 0; i < buffer.length; i++){
                dataPoints[i] = new DataPoint(dataCount++,buffer[i]);
            }

            packagesReceive++;
            baudRate = dataCount / packagesReceive;

            if(dataCount< baudRate *NUM_OF_BUFFER_DISPLAYS)
                viewport.setMinX(0);
            else
                viewport.setMinX(dataCount- baudRate *NUM_OF_BUFFER_DISPLAYS);


            viewport.setMaxX(dataCount);
            LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<DataPoint>(dataPoints);
            lineGraphSeries.setColor(Color.YELLOW);
            lineGraphSeries.setThickness(3);
            plotter.addSeries(lineGraphSeries);
            Log.d(TAG, "addEntry: Baudios ->" + baudRate);
            Log.d(TAG, "addEntry: Buffer to display ->" + NUM_OF_BUFFER_DISPLAYS);
            sendBaudRateToMain(baudRate);


        }catch (InterruptedException e){
            e.printStackTrace();

        }
    }
    //<----------------------------------------------------------------------------------->
    //<----------------------------------------------------------------------------------->
    //<----------------------------------------------------------------------------------->
    //<----------------------------------------------------------------------------------->

}
