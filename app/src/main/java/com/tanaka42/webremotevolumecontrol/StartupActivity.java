package com.tanaka42.webremotevolumecontrol;

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
    private Button mEnableDisableButton;
    private TextView mCloseHintTextView;
    private TextView mHowToTextView;
    private static String mServerURL = "";

    private BroadcastReceiver urlUpdatedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        getReadyToReceiveURLforUI();

        mCloseHintTextView = findViewById(R.id.textViewCloseWhenReady);
        mHowToTextView = findViewById(R.id.textViewHowTo);

        mURLTextView = findViewById(R.id.textViewURL);
        if (mServerURL.isEmpty()) {
            mURLTextView.setText(R.string.starting);
        } else {
            mURLTextView.setText(mServerURL);
        }

        mEnableDisableButton = findViewById(R.id.buttonEnableDisable);
        mEnableDisableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HttpServer.isStarted()) {
                    stopRemoteControlService();
                } else {
                    startRemoteControlService();
                }
            }
        });

        mCloseButton = findViewById(R.id.buttonClose);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        startRemoteControlService();
    }

    private void startRemoteControlService() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(this, ForegroundService.class));
        } else {
            startService(new Intent(this, ForegroundService.class));
        }
        mEnableDisableButton.setText(R.string.disable_volume_remote_control);
        mHowToTextView.setText(R.string.how_to_enabled);
        mURLTextView.setVisibility(View.VISIBLE);
        mCloseHintTextView.setVisibility(View.VISIBLE);
    }

    private void stopRemoteControlService() {
        stopService(new Intent(this, ForegroundService.class));
        mEnableDisableButton.setText(R.string.enable_volume_remote_control);
        mHowToTextView.setText(R.string.how_to_disabled);
        mURLTextView.setVisibility(View.INVISIBLE);
        mCloseHintTextView.setVisibility(View.INVISIBLE);
    }

    private void getReadyToReceiveURLforUI() {
        urlUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mServerURL = intent.getStringExtra("url");
                mURLTextView = findViewById(R.id.textViewURL);
                mURLTextView.setText(mServerURL);
            }
        };
        registerReceiver(urlUpdatedReceiver, new IntentFilter("com.tanaka42.webremotevolumecontrol.urlupdated"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(urlUpdatedReceiver);
    }
}