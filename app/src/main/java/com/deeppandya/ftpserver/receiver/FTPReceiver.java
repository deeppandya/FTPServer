package com.deeppandya.ftpserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deeppandya.ftpserver.service.FTPService;

public class FTPReceiver extends BroadcastReceiver {

    static final String TAG = FTPReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received: " + intent.getAction());

        try {
            if (intent.getAction().equals(FTPService.ACTION_START_FTPSERVER)) {
                Intent serverService = new Intent(context, FTPService.class);
                if (!FTPService.isRunning()) {
                    context.startService(serverService);
                }
            } else if (intent.getAction().equals(FTPService.ACTION_STOP_FTPSERVER)) {
                Intent serverService = new Intent(context, FTPService.class);
                context.stopService(serverService);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start/stop on intent " + e.getMessage());
        }
    }
}
