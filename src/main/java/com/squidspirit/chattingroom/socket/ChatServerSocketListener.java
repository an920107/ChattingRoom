package com.squidspirit.chattingroom.socket;

import org.json.JSONObject;

public interface ChatServerSocketListener {
    void onReceived(JSONObject jsonObject);
    void onFailed(Exception exception);
}
