package com.closerpilot.fonocardio_v3.Main.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.text.format.Formatter;

import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;


public class ThreadHttpServer extends Thread {
    private static final String TAG = "HttpServerThread";

    //Variable para saber si el hilo ya esta corriendo
    private static boolean threadHttpServerRunning = false;

    static String msgLog = "";

    public static String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                        break;
                    }
                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip = ("Something Wrong! " + e + "\n");
        }
        return ip;
    }
        /*
        StringBuilder ip = new StringBuilder();
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress())
                        ip.append(inetAddress.getHostAddress());
                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip.append("Something Wrong! ").append(e).append("\n");
        }
        return ip.toString();
    }
         */

    //Sabe si el hilo esta corriento
    public static boolean isThreadHttpServerRunning(){
        return threadHttpServerRunning;
    }


    //Lee el HTML y lo transforma a String
    private static String htmlToString(String fileName) throws IOException {
        InputStream inputStream = __PlugginControl__.context.getAssets().open(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
        StringBuilder out = new StringBuilder();
        final char[] buffer = new char[1024];
        int read;
        do {
            read = bufferedReader.read(buffer, 0, buffer.length);
            if (read > 0)
                out.append(buffer, 0, read);
        } while (read >= 0);

        return out.toString();
    }


    @Override
    public void run() {
        threadHttpServerRunning = true;

        __PlugginControl__.webSocketHttp = new WebSocketHttp(getIpAddress(), WEBSOCKET_PORT);
        __PlugginControl__.webSocketHttp.setReuseAddr(true);
        __PlugginControl__.webSocketHttp.start();

        try {
            __PlugginControl__.serverSocket = new ServerSocket(HTTP_SERVER_PORT);

            while(!ThreadHttpServer.currentThread().isInterrupted()){
                Socket socket = __PlugginControl__.serverSocket.accept();
                BufferedReader is;
                PrintWriter os;
                String request;

                //Ciclo para enviar el HTTP con el websocket
                while(!socket.isClosed()) {
                    try {
                        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        request = is.readLine();
                        os = new PrintWriter(socket.getOutputStream(), true);

                        String response = htmlToString("index.html");
                        os.print("HTTP/1.0 200" + "\r\n");
                        os.print("Content type: text/html" + "\r\n");
                        os.print("Content length: " + response.length() + "\r\n");
                        os.print("\r\n");
                        os.print(response + "\r\n");
                        os.flush();

                        socket.close();

                        msgLog = "Request of " + request + " from " + socket.getInetAddress().toString() + "\n";
                        toMainThreadHandlerMsg(msgLog);

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void toMainThreadHandlerMsg(String info){
        Message msg = Message.obtain();
        msg.what = HANDLER_SEND;
        msg.obj = info;
        __PlugginControl__.toMainHandler.sendMessage(msg);
    }
}
