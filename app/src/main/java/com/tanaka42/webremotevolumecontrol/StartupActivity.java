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
    private static String mServerIp = "";
    private static int mServerPort = 0;
    private static boolean mServerIpIsClassC = true;

    private BroadcastReceiver urlUpdatedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        getReadyToReceiveURLforUI();

        mCloseHintTextView   = findViewById(R.id.textViewCloseWhenReady);
        mHowToTextView       = findViewById(R.id.textViewHowTo);
        mURLTextView         = findViewById(R.id.textViewURL);
        mEnableDisableButton = findViewById(R.id.buttonEnableDisable);
        mCloseButton         = findViewById(R.id.buttonClose);

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

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        updateActivity();

        startRemoteControlService();
    }

    private void startRemoteControlService() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(this, ForegroundService.class));
        } else {
            startService(new Intent(this, ForegroundService.class));
        }
        updateActivity();
    }

    private void stopRemoteControlService() {
        stopService(new Intent(this, ForegroundService.class));
        updateActivity();
    }

    private void updateActivity() {
        if (HttpServer.isStarted()) {
            mEnableDisableButton.setText(R.string.disable_volume_remote_control);
            mHowToTextView.setText(R.string.how_to_enabled);
            mURLTextView.setText(mServerURL);
            mURLTextView.setVisibility(View.VISIBLE);
            mCloseHintTextView.setText(R.string.close_when_ready);
            mCloseHintTextView.setVisibility(View.VISIBLE);
        } else {
            mEnableDisableButton.setText(R.string.enable_volume_remote_control);
            if (mServerIpIsClassC) {
                mHowToTextView.setText(R.string.how_to_disabled);
                mCloseHintTextView.setVisibility(View.INVISIBLE);
                mURLTextView.setVisibility(View.INVISIBLE);
            } else {
                mCloseHintTextView.setVisibility(View.INVISIBLE);
                mURLTextView.setText(R.string.verify_local_network_connection);
                mURLTextView.setVisibility(View.VISIBLE);
                mCloseHintTextView.setVisibility(View.VISIBLE);
                mCloseHintTextView.setText(R.string.about_class_c_limitation);
                mHowToTextView.setText(R.string.how_to_unable_to_find_local_address);
            }
        }
    }

    private void getReadyToReceiveURLforUI() {
        urlUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mServerURL = intent.getStringExtra("url");
                mServerIp = intent.getStringExtra("ip");
                mServerPort = intent.getIntExtra("port", 0);
                mServerIpIsClassC = intent.getBooleanExtra("is_a_class_c_ip", true);
                updateActivity();
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