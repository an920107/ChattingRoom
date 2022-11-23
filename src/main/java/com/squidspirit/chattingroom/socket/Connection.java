package com.squidspirit.chattingroom.socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Connection implements Runnable {

    private ConnectionListener listener;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JSONObject jsonObject;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.jsonObject = new JSONObject();
    }

    @Override
    public void run() {
        if (socket == null || listener == null) return;
        boolean firstConnect = true;
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                if (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        jsonObject = new JSONObject(line);
                        if (firstConnect) {
                            listener.onConnected(jsonObject);
                            firstConnect = false;
                        }
                        else listener.onReceived(jsonObject);
                    }
                }
            }
        } catch (IOException | JSONException exception) {
            if (exception instanceof JSONException)
                listener.onFailed(exception);
        }
        listener.onDisconnected(jsonObject);
    }

    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }

    protected void send(JSONObject jsonObject) {
        if (socket.isClosed())
            return;
        Thread thread = new Thread(() -> {
            try {
                writer.write(jsonObject.toString());
                writer.write("\n");
                writer.flush();
            } catch (IOException exception) {
                listener.onFailed(exception);
                Connection.this.close();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException exception) {
            listener.onFailed(exception);
        }
    }

    protected void close() {
        try {
            socket.close();
        } catch (IOException exception) {
            listener.onFailed(exception);
        }
    }
}
