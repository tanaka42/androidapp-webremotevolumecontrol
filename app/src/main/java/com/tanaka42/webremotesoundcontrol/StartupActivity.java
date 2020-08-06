package com.tanaka42.webremotesoundcontrol;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartupActivity extends Activity {

    private TextView mURLTextView;
    private Button mCloseButton;
    private static String mServerURL = "";

    private BroadcastReceiver urlUpdatedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        urlUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mServerURL = intent.getStringExtra("url");
                mURLTextView = findViewById(R.id.textViewURL);
                mURLTextView.setText(mServerURL);
            }
        };
        registerReceiver(urlUpdatedReceiver, new IntentFilter("com.tanaka42.webremotesoundcontrol.urlupdated"));

        Context context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(new Intent(context, WebRemoteSoundControlService.class));
        } else {
            context.startService(new Intent(context, WebRemoteSoundControlService.class));
        }

        mURLTextView = findViewById(R.id.textViewURL);
        if (mServerURL.isEmpty()) {
            mURLTextView.setText(R.string.starting);
        } else {
            mURLTextView.setText(mServerURL);
        }

        mCloseButton = findViewById(R.id.buttonClose);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(urlUpdatedReceiver);
    }
}