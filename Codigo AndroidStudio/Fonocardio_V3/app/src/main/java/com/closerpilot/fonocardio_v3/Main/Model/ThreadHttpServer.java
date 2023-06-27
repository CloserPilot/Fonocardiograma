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
import android.os.Message;

import static com.closerpilot.fonocardio_v3.Main.Model.__Constants__.*;


public class ThreadHttpServer extends Thread {
    private static final String TAG = "HttpServerThread";

    // Boolean to know if current thread is running
    private static boolean threadHttpServerRunning = false;

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          PRIVATE          //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Read the HTML file and convert it to String
     * @param fileName The file name of the html file
     * @return The String of the html file
     * @throws IOException
     */
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

    /**
     * Send a message to the MainActivity
     */
    private static void toMainThreadHandlerMsg(String info){
        Message msg = Message.obtain();
        msg.what = HANDLER_SEND;
        msg.obj = info;
        __PlugginControl__.toMainHandler.sendMessage(msg);
    }

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          PUBLIC           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * Gets the firts IP address of the device
     * @return The IP Address
     */
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

    /**
     * Returns the state of the current Thread
     * @return {@code True} if Thread is running. {@code False} if otherwise.
     */
    public static boolean isThreadHttpServerRunning(){
        return threadHttpServerRunning;
    }

    //////////////////////////////////////////////////////
    /////////////                           //////////////
    /////////////          LOPPER           //////////////
    /////////////                           //////////////
    //////////////////////////////////////////////////////

    /**
     * If there is any HTTP request, send the "index.html" file.
     */
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

                //Send the HTTP response
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

                        String msgLog = "Request of " + request + " from " + socket.getInetAddress().toString() + "\n";
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
}
