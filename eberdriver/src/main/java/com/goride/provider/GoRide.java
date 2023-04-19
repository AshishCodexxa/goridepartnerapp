package com.goride.provider;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.goride.provider.models.singleton.CurrentTrip;
import com.goride.provider.utils.Const;
import com.goride.provider.utils.PreferenceHelper;
import com.goride.provider.utils.SocketHelper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

import org.json.JSONObject;

import io.socket.emitter.Emitter;

/**
 * Created by Ravi Bhalodi on 04,March,2020 in Elluminati
 */
public class GoRide extends Application {
    private IncomingHandler incomingHandler;
    private PreferenceHelper preferenceHelper;
    private boolean isRegisterTripSocket;
    /**
     * The Is have request.
     */
    /**
     * The Is background.
     */
    public static boolean isBackground;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (isBackground) {
//            if (isMyServiceRunning(FloatingViewService.class)) {
//                startService(new Intent(getApplicationContext(), FloatingViewService.class));
//            }
//        } else {
//            if (isMyServiceRunning(FloatingViewService.class)) {
//                stopService(new Intent(getApplicationContext(), FloatingViewService.class));
//            }
//        }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        incomingHandler = new IncomingHandler(this);
        preferenceHelper = PreferenceHelper.getInstance(getApplicationContext());
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.request_sound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel mChannel = new NotificationChannel("newTrip", "New Trip", NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setSound(sound, audioAttributes);
            notificationManager.createNotificationChannel(mChannel);
        }
        new Instabug.Builder(this, "11a5e9a169d8fb87876c30092e80bffc")
                .setInvocationEvents(InstabugInvocationEvent.SHAKE, InstabugInvocationEvent.NONE)
                .build();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity instanceof MainDrawerActivity) {
                    socketOnForNewRequest();
                }

            }

            @Override
            public void onActivityStarted(Activity activity) {
//                if(isMyServiceRunning(FloatingViewService.class)){
//                    stopService(new Intent(getApplicationContext(),FloatingViewService.class));
//                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                isBackground = false;
                if(isMyServiceRunning(FloatingViewService.class)){
                    stopService(new Intent(getApplicationContext(),FloatingViewService.class));
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                isBackground = true;
                if(!isMyServiceRunning(FloatingViewService.class) && activity.getClass().getSimpleName().equals("MainDrawerActivity")){
                    Log.e("Error", String.valueOf(activity.getClass().getSimpleName() +" :  com.elluminati.eber.driver.MainDrawerActivity"));

                    // Do Work
                            startService(new Intent(getApplicationContext(),FloatingViewService.class));

                }

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private boolean isMyServiceRunning(Class<FloatingViewService> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Socket on for new request.
     */
    public void socketOnForNewRequest() {
        if (!TextUtils.isEmpty(preferenceHelper.getSessionToken()) && !TextUtils.isEmpty(preferenceHelper.getProviderId()) && !isRegisterTripSocket) {
            isRegisterTripSocket = true;
            SocketHelper.getInstance().socketConnect();
            String newTrip = String.format("'%s%s'", SocketHelper.GET_NEW_REQUEST, preferenceHelper.getProviderId());
            SocketHelper.getInstance().getSocket().off(newTrip);
            SocketHelper.getInstance().getSocket().on(newTrip, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        JSONObject jsonObject = (JSONObject) args[0];
                        Message message = incomingHandler.obtainMessage();
                        message.obj = jsonObject;
                        incomingHandler.sendMessage(message);
                    }
                }
            });
        }
    }

    private static class IncomingHandler extends Handler {

        private final GoRide application;

        public IncomingHandler(GoRide application) {
            this.application = application;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            JSONObject jsonObject = (JSONObject) msg.obj;
            String tripId = jsonObject.optString(Const.Params.TRIP_ID, "");
            int timeLeft = jsonObject.optInt(Const.Params.TIME_LEFT_TO_RESPONDS_TRIP, 0);
            CurrentTrip.getInstance().setTimeLeft(timeLeft);
            application.preferenceHelper.putTripId(tripId);
            if (!TextUtils.isEmpty(application.preferenceHelper.getTripId()) && !TextUtils.isEmpty(application.preferenceHelper.getSessionToken())) {
                LocalBroadcastManager.getInstance(application).sendBroadcast(new Intent(Const.ACTION_NEW_TRIP));
            }
        }
    }
}
