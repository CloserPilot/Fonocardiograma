package com.closerpilot.fonocardio_v3.Main.Model;

import android.content.Context;
import android.os.Handler;

import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class __PlugginControl__ {
    //Context al UI Thread
    public static volatile Context context = null;

    //Buffers de datos
    public static volatile BlockingQueue<Short[]> bufferRawData = new LinkedBlockingQueue<>();
    public static volatile BlockingQueue<Short[]> bufferData = new LinkedBlockingQueue<>();

    //Handlers entre hilos
    public static volatile Handler toMainHandler = null;
    public static volatile Handler toThreadDataHandler = null;
    public static volatile Handler toThreadDataProcessHandler = null;

    //Hilos de programacion
    public static volatile ThreadData threadData = new ThreadData();
    public static volatile ThreadDataProcess threadDataProcess = new ThreadDataProcess();
    public static volatile ThreadHttpServer threadHttpServer = new ThreadHttpServer();

    //WebSocket
    public static volatile WebSocketHttp webSocketHttp = null;
    public static volatile ServerSocket serverSocket;
}
