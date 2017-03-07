package com.deeppandya.ftpserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.deeppandya.ftpserver.service.FTPService;

public class MainActivity extends AppCompatActivity {

    TextView statusText, warningText, ftpAddrText;
    Button ftpBtn;
    private int skin_color, skinTwoColor;

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                warningText.setText("");
            } else {
                stopServer();
                statusText.setText(getResources().getString(R.string.ftp_status_not_running));
                warningText.setText(getResources().getString(R.string.ftp_no_wifi));
                ftpAddrText.setText("");
                ftpBtn.setText(getResources().getString(R.string.start_ftp));
            }
        }
    };
    private BroadcastReceiver ftpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == FTPService.ACTION_STARTED) {
                statusText.setText(getResources().getString(R.string.ftp_status_running));
                warningText.setText("");
                ftpAddrText.setText(getFTPAddressString());
                ftpBtn.setText(getResources().getString(R.string.stop_ftp));
            } else if (action == FTPService.ACTION_FAILEDTOSTART) {
                statusText.setText(getResources().getString(R.string.ftp_status_not_running));
                warningText.setText("Oops! Something went wrong");
                ftpAddrText.setText("");
                ftpBtn.setText(getResources().getString(R.string.start_ftp));
            } else if (action == FTPService.ACTION_STOPPED) {
                statusText.setText(getResources().getString(R.string.ftp_status_not_running));
                ftpAddrText.setText("");
                ftpBtn.setText(getResources().getString(R.string.start_ftp));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText =(TextView) findViewById(R.id.statusText);
        warningText = (TextView) findViewById(R.id.warningText);
        ftpAddrText = (TextView) findViewById(R.id.ftpAddressText);
        ftpBtn = (Button) findViewById(R.id.startStopButton);

        ImageView ftpImage = (ImageView)findViewById(R.id.ftp_image);

        ftpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!FTPService.isRunning()) {
                    if (FTPService.isConnectedToWifi(MainActivity.this))
                        startServer();
                    else
                        warningText.setText(getResources().getString(R.string.ftp_no_wifi));
                } else {
                    stopServer();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, wifiFilter);
        IntentFilter ftpFilter = new IntentFilter();
        ftpFilter.addAction(FTPService.ACTION_STARTED);
        ftpFilter.addAction(FTPService.ACTION_STOPPED);
        ftpFilter.addAction(FTPService.ACTION_FAILEDTOSTART);
        registerReceiver(ftpReceiver, ftpFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(ftpReceiver);
    }

    /**
     * Sends a broadcast to start ftp server
     */
    private void startServer() {
        sendBroadcast(new Intent(FTPService.ACTION_START_FTPSERVER));
    }

    /**
     * Sends a broadcast to stop ftp server
     */
    private void stopServer() {
        sendBroadcast(new Intent(FTPService.ACTION_STOP_FTPSERVER));
    }

    /**
     * Update UI widgets based on connection status
     */
    private void updateStatus() {
        if (FTPService.isRunning()) {
            statusText.setText(getResources().getString(R.string.ftp_status_running));
            ftpBtn.setText(getResources().getString(R.string.stop_ftp));
            ftpAddrText.setText(getFTPAddressString());
        } else {
            statusText.setText(getResources().getString(R.string.ftp_status_not_running));
            ftpBtn.setText(getResources().getString(R.string.start_ftp));
        }
    }

    /**
     * @return address at which server is running
     */
    private String getFTPAddressString() {
        return "ftp://" + FTPService.getLocalInetAddress(MainActivity.this).getHostAddress() + ":" + FTPService.getPort();
    }

}
