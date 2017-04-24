package com.rxconnectionstatusview;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.kevadiyakrunalk.rxconnection.ConnectionManager;
import com.kevadiyakrunalk.rxconnection.StatusView;

public class MainActivity extends AppCompatActivity implements StatusView.TimerListener {
    private StatusView statusView;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        statusView = (StatusView) findViewById(R.id.status);

        connectionManager = new ConnectionManager.Builder()
                .setContext(this)
                .setStatusView(statusView)
                .build();
        statusView.setOnTimeChangeListener(this);
    }

    @Override
    public void onConnectionRetry() {
        statusView.cancel();
        if (!connectionManager.hasNetwork) {
            statusView.setStartCount(2);
            statusView.start();
        }
    }

    @Override
    public void OnTimeChanged(boolean isFinished) {
        statusView.continueTimer();
    }

    @Override
    public void onConnectionStatus(boolean isConnected) {
        if(isConnected)
            Toast.makeText(this, "do some tasks", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "stop some tasks", Toast.LENGTH_SHORT).show();
    }
}
