package com.closerpilot.fonocardio_v3.Main.Model.Plugins;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class myWebSocket  extends WebSocketServer {
    private static final String TAG = "myWebSocket";
    int contador = 0;

    public myWebSocket(String address, int port) {
        super(new InetSocketAddress(address, port));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        Log.d(TAG, "onError: " + e);

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Server started");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        broadcast("new connection: " + contador); //This method sends a message to all clients connected
        contador++;
    }
}
