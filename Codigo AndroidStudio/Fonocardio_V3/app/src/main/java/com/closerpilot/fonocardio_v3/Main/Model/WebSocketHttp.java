package com.closerpilot.fonocardio_v3.Main.Model;

import android.util.Log;

import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;



public class WebSocketHttp extends WebSocketServer {
    private static final String TAG = "myWebSocket";

    public WebSocketHttp(String address, int port) {
        super(new InetSocketAddress(address, port));
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Server started");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onMessage(org.java_websocket.WebSocket conn, String message) {
    }

    @Override
    public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onError(org.java_websocket.WebSocket conn, Exception e) {
        Log.d(TAG, "onError: " + e);
    }
}
