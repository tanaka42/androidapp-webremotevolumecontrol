package com.tanaka42.webremotesoundcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TranslucentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        context.startForegroundService(new Intent(context, WebRemoteSoundControlService.class));
        finish();
    }
}