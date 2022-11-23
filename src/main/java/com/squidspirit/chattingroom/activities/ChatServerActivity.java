package com.squidspirit.chattingroom.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.squidspirit.chattingroom.R;
import com.squidspirit.chattingroom.socket.ChatServerSocket;
import com.squidspirit.chattingroom.socket.ChatServerSocketListener;
import com.squidspirit.chattingroom.time.CurrentTime;

import org.json.JSONObject;

public class ChatServerActivity extends AppCompatActivity {

    private LinearLayout messageLinearLayout;
    private ActionBar actionBar;

    private ChatServerSocket socket;
    private String room;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        messageLinearLayout = findViewById(R.id.serverMessageLinearLayout);

        try {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            room = bundle.getString("room");
            port = bundle.getInt("port");
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        socket = new ChatServerSocket(port, room);
        socket.setListener(new ChatServerSocketListener() {
            @Override
            public void onReceived(JSONObject jsonObject) {
                runOnUiThread(() -> updateMessage(jsonObject));
            }

            @Override
            public void onFailed(Exception exception) {
                exception.printStackTrace();
            }
        });
        new Thread(socket).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            socket.close();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMessage(JSONObject jsonObject) {
        TextView textView;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 30);
        try {
            if (!jsonObject.getBoolean("system")) {
                textView = new TextView(this);
                textView.setText(jsonObject.getString("sender"));
                textView.setTextSize(24);
                messageLinearLayout.addView(textView);
            }
            textView = new TextView(this);
            textView.setText(jsonObject.getString("message"));
            messageLinearLayout.addView(textView);
            textView = new TextView(this);
            textView.setText(CurrentTime.getTime());
            textView.setLayoutParams(params);
            messageLinearLayout.addView(textView);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
