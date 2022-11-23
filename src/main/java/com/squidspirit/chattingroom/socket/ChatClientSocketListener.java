package com.squidspirit.chattingroom.socket;

import org.json.JSONObject;

public interface ChatClientSocketListener {
    void onConnected(JSONObject jsonObject);
    void onReceived(JSONObject jsonObject);
    void onDisconnected(JSONObject jsonObject);
    void onFailed(Exception exception);
}
