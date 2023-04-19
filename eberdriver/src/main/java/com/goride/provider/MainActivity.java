package com.goride.provider;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.goride.provider.components.CustomLanguageDialog;
import com.goride.provider.components.MyFontTextView;
import com.goride.provider.utils.SocketHelper;
import com.goride.provider.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends BaseAppCompatActivity {

    private MyFontTextView btnSignIn, btnRegister, tvVersion;
    private CustomLanguageDialog customLanguageDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
//        final Window win = getWindow();
//        win.addFlags(WindowManag er.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        SocketHelper.getInstance().socketDisconnect();
        stopService(new Intent(this, EberUpdateService.class));
        tvVersion = findViewById(R.id.tvVersion);
        btnSignIn = findViewById(R.id.tvSingIn);
        btnRegister = findViewById(R.id.tvRegister);
        btnRegister.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        findViewById(R.id.tvChangeLanguage).setOnClickListener(this);
        checkPlayServices();
        tvVersion.setText(String.format("%s %s", getResources().getString(R.string.text_version), Utils.getAppVersion(MainActivity.this)));
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 12).show();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSingIn:
                goToSignInActivity();
                break;
            case R.id.tvRegister:
                goToRegisterActivity();
                break;
            case R.id.tvChangeLanguage:
                openLanguageDialog();
            default:

                break;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
    }

    @Override
    protected void onStart() {
        if(isMyServiceRunning(FloatingViewService.class)){
            stopService(new Intent(MainActivity.this,FloatingViewService.class));
        }
        super.onStart();
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

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogInternet();
        } else {
            openInternetDialog();
        }
    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogGps();
        } else {
            openGpsDialog();

        }
    }

    private void openLanguageDialog() {
        if (customLanguageDialog != null && customLanguageDialog.isShowing()) {
            return;
        }
        customLanguageDialog = new CustomLanguageDialog(MainActivity.this) {
            @Override
            public void onSelect(String languageName, String languageCode) {
                //  tvLanguage.setText(languageName);
                if (!TextUtils.equals(preferenceHelper.getLanguageCode(), languageCode)) {
                    preferenceHelper.putLanguageCode(languageCode);
                    finishAffinity();
                    restartApp();
                }
                dismiss();
            }
        };
        customLanguageDialog.show();
    }

    @Override
    public void onAdminApproved() {
        goWithAdminApproved();
    }

    @Override
    public void onAdminDeclined() {
        goWithAdminDecline();
    }

//    @Override
//    protected void onUserLeaveHint()
//    {
//        // When user presses home page
//        startService(new Intent(MainActivity.this,FloatingViewService.class));
//
//        super.onUserLeaveHint();
//    }
}


