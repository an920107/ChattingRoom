package com.squidspirit.chattingroom.socket;

import com.squidspirit.chattingroom.time.CurrentTime;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

public class ChatServerSocket implements Runnable{

    private final int TIMEOUT = 10000;

    private ChatServerSocketListener listener;
    public void setListener(ChatServerSocketListener listener) {
        this.listener = listener;
    }

    private ServerSocket serverSocket;
    private Socket socket;
    private ArrayList<Connection> connections;
    private String room;
    private int port;

    public ChatServerSocket(int port, String room) {
        this.listener = null;
        this.socket = new Socket();
        this.connections = new ArrayList<>();
        this.room = room;
        this.port = port;
    }

    @Override
    public void run() {
        if (listener == null) return;
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()) {
                socket = serverSocket.accept();
                socket.setSoTimeout(TIMEOUT);

                Connection connection = new Connection(socket);
                connections.add(connection);
                connection.setListener(new ConnectionListener() {
                    @Override
                    public void onConnected(JSONObject jsonObject) {
                        JSONObject toSend;
                        try {
                            String sender = jsonObject.getString("sender");
                            toSend = generateJson(
                                    sender, true, String.format("%s has joined.", sender));
                            sendAll(toSend);
                            listener.onReceived(toSend);
                        } catch (JSONException exception) {
                            listener.onFailed(exception);
                        }
                    }

                    @Override
                    public void onReceived(JSONObject jsonObject) {
                        JSONObject toSend;
                        try {
                            int status = jsonObject.getInt("status");
                            if (status == OnlineStatus.UPDATE.getVal())
                                return;
                            if (status == OnlineStatus.DISCONNECT.getVal()) {
                                onDisconnected(jsonObject);
                                return;
                            }
                            String sender = jsonObject.getString("sender");
                            String message = jsonObject.getString("message");
                            toSend = generateJson(sender, false, message);
                            sendAll(toSend);
                            listener.onReceived(toSend);
                        } catch (JSONException exception) {
                            listener.onFailed(exception);
                        }
                    }

                    @Override
                    public void onDisconnected(JSONObject jsonObject) {
                        JSONObject toSend;
                        try {
                            String sender = jsonObject.getString("sender");
                            toSend = generateJson(
                                    sender, true, String.format("%s has left.", sender));
                            sendAll(toSend);
                            listener.onReceived(toSend);
                            connections.remove(connection);
                        } catch (JSONException exception) {
                            listener.onFailed(exception);
                        }
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        listener.onFailed(exception);
                        connections.remove(connection);
                    }
                });
                new Thread(connection).start();
            }
        } catch (Exception exception) {
            listener.onFailed(exception);
        }
    }

    private JSONObject generateJson(String sender, boolean system, String message) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("room", room);
        jsonObject.put("sender", sender);
        jsonObject.put("system", system);
        jsonObject.put("time", CurrentTime.getTime());
        jsonObject.put("message", message);
        return jsonObject;
    }

    public void sendAll(JSONObject jsonObject) {
        for (Connection conn : connections)
            conn.send(jsonObject);
    }

    public void close() {
        try {
            for (Connection conn : connections)
                conn.close();
            serverSocket.close();
        } catch (IOException exception) {
            listener.onFailed(exception);
        }
    }

    public String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkEnumeration.hasMoreElements()) {
                NetworkInterface network = networkEnumeration.nextElement();
                Enumeration<InetAddress> ipEnumeration = network.getInetAddresses();
                while (ipEnumeration.hasMoreElements()) {
                    InetAddress inetAddress = ipEnumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        String ip = inetAddress.getHostAddress();
                        if (ip != null && ip.split("\\.").length == 4)
                            return ip;
                    }
                }
            }
        } catch (SocketException exception) {
            listener.onFailed(exception);
        }
        return null;
    }
}
