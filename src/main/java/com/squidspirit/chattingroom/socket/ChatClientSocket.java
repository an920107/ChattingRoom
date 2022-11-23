package com.squidspirit.chattingroom.socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.Socket;

public class ChatClientSocket implements Runnable {

    private final int TIMEOUT = 1000;
    private final int UPDATE_TIME = 6000;

    private ChatClientSocketListener listener;
    public void setListener(ChatClientSocketListener listener) {
        this.listener = listener;
    }

    private String host;
    private int port;
    private String name;

    private Socket socket;
    private Connection connection;

    public ChatClientSocket(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.socket = new Socket();
        this.listener = null;
    }

    @Override
    public void run() {
        if (listener == null) return;
        try {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            connection = new Connection(socket);
            connection.setListener(new ConnectionListener() {
                @Override
                public void onConnected(JSONObject jsonObject) {
                    listener.onConnected(jsonObject);
                }

                @Override
                public void onReceived(JSONObject jsonObject) {
                    listener.onReceived(jsonObject);
                }

                @Override
                public void onDisconnected(JSONObject jsonObject) {
                    listener.onDisconnected(jsonObject);
                }

                @Override
                public void onFailed(Exception exception) {
                    listener.onFailed(exception);
                }
            });
            new Thread(connection).start();

            long lastTime = 0;
            do {
                if (System.currentTimeMillis() - lastTime > UPDATE_TIME) {
                    connection.send(generateJson(name, OnlineStatus.UPDATE, ""));
                    lastTime = System.currentTimeMillis();
                }
            } while (socket.isConnected() && !socket.isClosed());
            ChatClientSocket.this.close();
        } catch (Exception exception) {
            listener.onFailed(exception);
        }
    }

    private JSONObject generateJson(String sender, OnlineStatus status, String message) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", sender);
        jsonObject.put("status", status.getVal());
        jsonObject.put("message", message);
        return jsonObject;
    }

    public void send(String message) {
        try {
            connection.send(generateJson(name, OnlineStatus.SENDING, message));
        } catch (JSONException exception) {
            listener.onFailed(exception);
        }
    }

    public void close() {
        try {
            connection.send(generateJson(name, OnlineStatus.DISCONNECT, ""));
            connection.close();
            listener.onDisconnected(null);
        } catch (JSONException | NullPointerException exception) {
            listener.onFailed(exception);
        }
    }
}
