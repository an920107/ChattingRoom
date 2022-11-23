package com.squidspirit.chattingroom.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.squidspirit.chattingroom.R;

public class MainActivity extends AppCompatActivity {

    private Button connectButton;
    private Button createServerButton;
    private EditText hostEditText;
    private EditText portEditText;
    private EditText nameEditText;
    private EditText roomNameEditText;
    private EditText serverPortEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        createServerButton = findViewById(R.id.createServerButton);
        hostEditText = findViewById(R.id.hostEditText);
        portEditText = findViewById(R.id.portEditText);
        nameEditText = findViewById(R.id.nameEditText);
        roomNameEditText = findViewById(R.id.roomNameEditText);
        serverPortEditText = findViewById(R.id.serverPortEditText);

        connectButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("host", hostEditText.getText().toString());
            try {
                bundle.putInt("port", Integer.parseInt(portEditText.getText().toString()));
            } catch (Exception ignored) { return; }
            bundle.putString("name", nameEditText.getText().toString());
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(MainActivity.this, ChatClientActivity.class);
            startActivity(intent);
        });

        createServerButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("room", roomNameEditText.getText().toString());
            try {
                bundle.putInt("port", Integer.parseInt(serverPortEditText.getText().toString()));
            } catch (Exception ignored) { return; }
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(MainActivity.this, ChatServerActivity.class);
            startActivity(intent);
        });
    }
}