package com.squidspirit.chattingroom.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.squidspirit.chattingroom.R;
import com.squidspirit.chattingroom.socket.ChatClientSocket;
import com.squidspirit.chattingroom.socket.ChatClientSocketListener;

import org.json.JSONObject;

public class ChatClientActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText messageEditText;
    private LinearLayout messageLinearLayout;
    private ActionBar actionBar;

    private ChatClientSocket socket;
    private String host;
    private int port;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        messageLinearLayout = findViewById(R.id.messageLinearLayout);

        try {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            host = bundle.getString("host");
            port = bundle.getInt("port");
            name = bundle.getString("name");
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        socket = new ChatClientSocket(host, port, name);
        socket.setListener(new ChatClientSocketListener() {
            @Override
            public void onConnected(JSONObject jsonObject) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatClientActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    updateTitle(jsonObject);
                });
            }

            @Override
            public void onReceived(JSONObject jsonObject) {
                runOnUiThread(() -> {
                    updateTitle(jsonObject);
                    updateMessage(jsonObject);
                });
            }

            @Override
            public void onDisconnected(JSONObject jsonObject) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatClientActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailed(Exception exception) {
                exception.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ChatClientActivity.this, "Lost Connection", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
        new Thread(socket).start();

        sendButton.setOnClickListener(v -> socket.send(messageEditText.getText().toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            socket.close();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTitle(JSONObject jsonObject) {
        try {
            actionBar.setTitle(jsonObject.getString("room"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void updateMessage(JSONObject jsonObject) {
        TextView textView;
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 30);
        messageEditText.setText("");
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
            textView.setText(jsonObject.getString("time"));
            textView.setLayoutParams(params);
            messageLinearLayout.addView(textView);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
