package com.goride.provider.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.goride.provider.MainActivity;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK+
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT +
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION +
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        context.startActivity(i);      }
}
