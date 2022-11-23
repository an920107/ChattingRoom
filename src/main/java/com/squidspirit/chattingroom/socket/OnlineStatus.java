package com.squidspirit.chattingroom.socket;

public enum OnlineStatus {
    DISCONNECT(-1),
    UPDATE(0),
    SENDING(1);

    private final int val;

    OnlineStatus(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
