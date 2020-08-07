package com.tanaka42.webremotevolumecontrol;

import android.Manifest;
import android.annotation.NonNull;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
        registerReceiver(urlUpdatedReceiver, new IntentFilter("com.tanaka42.webremotevolumecontrol.urlupdated"));

        Context context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(new Intent(context, ForegroundService.class));
        } else {
            context.startService(new Intent(context, ForegroundService.class));
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

        createShortcut();
    }

    private boolean hasCreateShortcutPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INSTALL_SHORTCUT) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                System.out.println("Calling requestPermissions...");
                requestPermissions(new String[] { Manifest.permission.INSTALL_SHORTCUT }, 42);
                return false;
            }
        } else {
            return true;
        }
    }

    private void createShortcut() {
        System.out.println("Adding shortcut...");
        if (hasCreateShortcutPermission()) {
            Context context = getApplicationContext();
            Intent shortcutIntent = new Intent(context, StartupActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher));
            addIntent.putExtra("duplicate", false);
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            context.sendBroadcast(addIntent);
            System.out.println("Shortcut added.");
        } else {
            System.out.println("Does not have permission do add a shortcut.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 42:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createShortcut();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(urlUpdatedReceiver);
    }
}