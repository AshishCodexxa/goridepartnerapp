package com.goride.provider.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.goride.provider.EberUpdateService;
import com.goride.provider.utils.PreferenceHelper;


public class AfterBootStartLocationUpdateReceiver extends BroadcastReceiver {
    public PreferenceHelper preferenceHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        preferenceHelper = PreferenceHelper.getInstance(context);
        if (!TextUtils.isEmpty(preferenceHelper.getSessionToken())) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Intent serviceIntent = new Intent(context, EberUpdateService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
