package com.goride.provider;

import static com.goride.provider.utils.Const.REQUEST_UPDATE_APP;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goride.provider.adapter.CircularProgressViewAdapter;
import com.goride.provider.adapter.DrawerAdapter;
import com.goride.provider.components.CustomCircularProgressView;
import com.goride.provider.components.CustomDialogBigLabel;
import com.goride.provider.components.CustomDialogEnable;
import com.goride.provider.components.CustomLanguageDialog;
import com.goride.provider.components.LocationProminentDisclosureDialog;
import com.goride.provider.components.MyFontTextView;
import com.goride.provider.components.TopSheet.TopSheetBehavior;
import com.goride.provider.fragments.FeedbackFragment;
import com.goride.provider.fragments.InvoiceFragment;
import com.goride.provider.fragments.MapFragment;
import com.goride.provider.fragments.ReceiveNewTripFragment;
import com.goride.provider.fragments.TripFragment;
import com.goride.provider.interfaces.ClickListener;
import com.goride.provider.interfaces.RecyclerTouchListener;
import com.goride.provider.models.datamodels.AdminSettings;
import com.goride.provider.models.responsemodels.SettingsDetailsResponse;
import com.goride.provider.models.singleton.CurrentTrip;
import com.goride.provider.parse.ApiClient;
import com.goride.provider.parse.ApiInterface;
import com.goride.provider.R;
import com.goride.provider.utils.AppLog;
import com.goride.provider.utils.Const;
import com.goride.provider.utils.ImageHelper;
import com.goride.provider.utils.KalmanLatLong;
import com.goride.provider.utils.LocationHelper;
import com.goride.provider.utils.SpacesItemDecoration;
import com.goride.provider.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.stripe.android.PaymentAuthConfig;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainDrawerActivity extends BaseAppCompatActivity implements LocationHelper.OnLocationReceived {

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 304;
    public Location currentLocation;
    private CustomLanguageDialog customLanguageDialog;

    public Location lastLocation;
    public LocationHelper locationHelper;
    public CurrentTrip currentTrip;
    public int countUpdateForLocation = 0;
    public ImageHelper imageHelper;
    public DrawerAdapter drawerAdapter;
    public Stripe stripe;
    /**
     * this interval define provider location update time
     */
    //private DrawerLayout drawerLayout;
    private RecyclerView recycleView;
    private LocationReceivedListener locationReceivedListener;
    private int drawerItemPosition;
    private CustomDialogBigLabel customDialogBigLabel, locationSettingRecommendDialog;
    private CustomDialogEnable customDialogEnable;
    private Dialog dialogProgress;
    private CustomDialogBigLabel customCancelTripDialog;
    private KalmanLatLong kalmanLatLong;
    private LinearLayout llDrawerBg;
    private TopSheetBehavior topSheetBehavior;
    private NetworkListener networkListener;
    private LocationProminentDisclosureDialog prominentDisclosureDialog;
    private FrameLayout view;
    private AppUpdateManager appUpdateManager;

    public void setNetworkListener(NetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    public void setLocationListener(LocationReceivedListener locationReceivedListener) {
        this.locationReceivedListener = locationReceivedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setTurnScreenOn(true);
            this.setShowWhenLocked(true);
        }
        appUpdateManager = AppUpdateManagerFactory.create(MainDrawerActivity.this);
        getAPIKeys();
        currentTrip = CurrentTrip.getInstance();
        locationHelper = new LocationHelper(this);
        locationHelper.setLocationReceivedLister(this);
        imageHelper = new ImageHelper(this);
        view = findViewById(R.id.frameMap);
        initToolBar();
        initDrawer();
        if (!TextUtils.isEmpty(preferenceHelper.getStripePublicKey())) {
            initStripePayment();
        }
        kalmanLatLong = new KalmanLatLong(25);
        /*if (checkLocationPermission()) {
            loadFragmentsAccordingStatus();
        }*/
        locationHelper.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
//        if(isMyServiceRunning(FloatingViewService.class)){
//            stopService(new Intent(MainDrawerActivity.this,FloatingViewService.class));
//        }
    }


    @Override
    protected void onPause() {
        super.onPause();
//  startService(new Intent(MainDrawerActivity.this,FloatingViewService.class));
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
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
        drawerOpen();
    }

    @Override
    public void onClick(View v) {
    }

    protected void openLogoutDialog() {
        customDialogBigLabel = new CustomDialogBigLabel(this, getString(R.string.text_logout), getString(R.string.msg_are_you_sure), getString(R.string.text_yes), getString(R.string.text_no)) {
            @Override
            public void positiveButton() {
                customDialogBigLabel.dismiss();
                logOut(false);
            }

            @Override
            public void negativeButton() {
                customDialogBigLabel.dismiss();
            }
        };

        if (!isFinishing() && !customDialogBigLabel.isShowing()) {
            customDialogBigLabel.show();
        }
    }

    private void initDrawer() {
        LinearLayout llDrawer = findViewById(R.id.llDrawer);
        llDrawerBg = findViewById(R.id.llDrawerBg);
        topSheetBehavior = TopSheetBehavior.from(llDrawer);
        topSheetBehavior.setTopSheetCallback(new TopSheetBehavior.TopSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                llDrawerBg.setClickable(topSheetBehavior.getState() == TopSheetBehavior.STATE_EXPANDED);

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                llDrawerBg.setAlpha(slideOffset);
                llDrawerBg.setVisibility(slideOffset == 0 ? View.GONE : View.VISIBLE);


            }
        });
        findViewById(R.id.ivClosedDrawerMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerClosed();

            }
        });
        findViewById(R.id.btnLogOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerClosed();
                openLogoutDialog();
            }
        });
        llDrawerBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerClosed();
            }
        });
        drawerAdapter = new DrawerAdapter(this);
        recycleView = findViewById(R.id.listViewDrawer);
        recycleView.setAdapter(drawerAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelOffset(R.dimen.dimen_bill_line)));
        recycleView.addOnItemTouchListener(new RecyclerTouchListener(this, recycleView, new ClickListener() {
            @Override
            public void onLongClick(View view, int position) {

            }

            @Override
            public void onClick(View view, int position) {
                drawerItemPosition = position;
                switch (drawerItemPosition) {
                    case 0:
                        goToProfileActivity();
                        break;
                    case 1:
                        goToHistoryActivity();
                        break;
                    case 2:
                        goToDocumentActivity(true);
                        break;
                    case 3:
                        goToShowReferralActivity();
                        break;
                    case 4:
                        goToSettingsActivity();
                        break;
                    case 5:
                        goToEarningActivity();
                        break;
                    case 6:
                        goToPaymentActivity();
                        break;
                    case 7:
                        goToContactUsActivity();
                        break;
                    case 8:
                        openLanguageDialog();
//                        gotoBankDetailActivity();
                        break;

                    case 9:
                        openLanguageDialog();
                        break;
                    default:
                        break;
                }
            }
        }));
    }
    private void openLanguageDialog() {
        if (customLanguageDialog != null && customLanguageDialog.isShowing()) {
            return;
        }
        customLanguageDialog = new CustomLanguageDialog(MainDrawerActivity.this) {
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

    private void loadFragmentsAccordingStatus() {
        Bundle extras = getIntent().getExtras();
        boolean isFromNotification;
        if (extras != null) {
            isFromNotification = extras.getBoolean(Const.Params.IS_FROM_NOTIFICATION);
        } else {
            isFromNotification = false;
        }
        if (TextUtils.isEmpty(preferenceHelper.getTripId()) || isFromNotification) {
            goToMapFragment(isFromNotification);
        } else if (!TextUtils.isEmpty(preferenceHelper.getNearDestinationTripId())) {
            goToReceiveNewTripFragment(preferenceHelper.getNearDestinationTripId());
        } else {
            goToTripFragment();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                CustomDialogEnable customDialogEnableGps = new CustomDialogEnable(this, getText(R.string.msg_app_overlay_permission), getString(R.string.text_no), getString(R.string.text_yes)) {
                    @Override
                    public void doWithEnable() {

//                        if (Build.MANUFACTURER.equals("Xiaomi")) {
//                            try {
//                                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//                                intent.setClassName("com.miui.sercuritycenter","com.miui.permcenter.permissions.PermissionsEditorActivity");
//                                intent.putExtra("extra_pkgname",getPackageName());
//                                startActivity(intent);
//                                return;
//                            } catch (Exception ignore){
//                                Log.d("Xiaomi", "first fails");
//                            }
//                            try {
//                                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//                                intent.setClassName("com.miui.sercuritycenter","com.miui.permcenter.permissions.AppPermissionsEditorActivity");
//                                intent.putExtra("extra_pkgname", getPackageName());
//                                startActivity(intent);
//                                return;
//                            } catch (Exception ignore){
//                                Log.d("Xiaomi", "second fails");
//                            }
//
//                        } else {
//                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                                    Uri.parse("package:" + getPackageName()));
//                            startActivityForResult(intent, 2084);
//                        }

                        if ("xiaomi".equals(Build.MANUFACTURER.toLowerCase(Locale.ROOT))) {
                            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            intent.setClassName("com.miui.securitycenter",
                                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            intent.putExtra("extra_pkgname", getPackageName());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                        }
                    }

                    @Override
                    public void doWithDisable() {

                    }
                };
                customDialogEnableGps.show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        locationFilter(currentLocation);
        if (locationReceivedListener != null) {
            locationReceivedListener.onLocationReceived(location);
        }
    }

    private void getAPIKeys() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, !TextUtils.isEmpty(preferenceHelper.getSessionToken()) ? preferenceHelper.getProviderId() : null);
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.APP_VERSION, Utils.getAppVersion(MainDrawerActivity.this));
            jsonObject.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
            Call<SettingsDetailsResponse> call = ApiClient.getClient().create(ApiInterface.class).getProviderSettingDetail(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<SettingsDetailsResponse>() {
                @Override
                public void onResponse(Call<SettingsDetailsResponse> call, Response<SettingsDetailsResponse> response) {
                    if (parseContent.isSuccessful(response)) {
                        parseContent.parseProviderSettingDetail(response.body());
                        final AdminSettings adminSettings = response.body().getAdminSettings();
                        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
                        appUpdateInfoTask.addOnSuccessListener(result -> {
                            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                                openUpdateAppDialog(result, adminSettings.isAndroidProviderAppForceUpdate());
                            } else if (Utils.checkVersionCode(MainDrawerActivity.this, adminSettings.getAndroidProviderAppVersionCode())) {
                                openUpdateAppDialog(null, adminSettings.isAndroidProviderAppForceUpdate());
                            } else {
                                if (checkLocationPermission()) {
                                    loadFragmentsAccordingStatus();
                                }
                            }
                        }).addOnFailureListener(e -> {
                            AppLog.handleThrowable(SplashScreenActivity.class.getSimpleName(), e);
                            if (Utils.checkVersionCode(MainDrawerActivity.this, adminSettings.getAndroidProviderAppVersionCode())) {
                                openUpdateAppDialog(null, adminSettings.isAndroidProviderAppForceUpdate());
                            } else {
                                if (checkLocationPermission()) {
                                    loadFragmentsAccordingStatus();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<SettingsDetailsResponse> call, Throwable t) {
                    AppLog.handleThrowable(TAG, t);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openUpdateAppDialog(final AppUpdateInfo result, final boolean isForceUpdate) {
        if (isForceUpdate) {
            final CustomDialogBigLabel customDialogBigLabel = new CustomDialogBigLabel(this, getResources().getString(R.string.text_update_app), getResources().getString(R.string.meg_update_app), getResources().getString(R.string.text_update), getResources().getString(R.string.text_exit_caps)) {
                @Override
                public void positiveButton() {
                    if (result != null) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, MainDrawerActivity.this, REQUEST_UPDATE_APP);
                        } catch (IntentSender.SendIntentException e) {
                            AppLog.handleException(SplashScreenActivity.class.getSimpleName(), e);
                        }
                        dismiss();
                    } else {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" + ".com/store/apps/details?id=" + getPackageName())));
                        }
                        dismiss();
                        finishAffinity();
                    }
                }

                @Override
                public void negativeButton() {
                    dismiss();
                    if (isForceUpdate) {
                        finishAffinity();
                    }
                }
            };
            if (!isFinishing()) {
                customDialogBigLabel.show();
            }
        } else {
            if (checkLocationPermission()) {
                loadFragmentsAccordingStatus();
            }
        }
    }

    private void goToHistoryActivity() {
        Intent historyIntent = new Intent(MainDrawerActivity.this, TripHistoryActivity.class);
        startActivity(historyIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToProfileActivity() {
        Intent intent = new Intent(MainDrawerActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToContactUsActivity() {
        Intent intent = new Intent(MainDrawerActivity.this, ContactUsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PowerManager powerManager = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        if (powerManager.isPowerSaveMode()) {
            showServerDialog();
        }


        preferenceHelper.putIsMainScreenVisible(true);
        countUpdateForLocation = preferenceHelper.getCheckCountForLocation();
    }
//    private boolean isMyServiceRunning(Class<FloatingViewService> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void showServerDialog() {
        final Dialog serverDialog = new Dialog(this);
        serverDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        serverDialog.setContentView(R.layout.dialog_batter_warning);
        WindowManager.LayoutParams params = serverDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        serverDialog.getWindow().setAttributes(params);
        serverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        serverDialog.setCancelable(false);
        serverDialog.show();

        Button btn = serverDialog.findViewById(R.id.btnDialogDisablePowerSaver);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerManager powerManager = (PowerManager)
                        getSystemService(Context.POWER_SERVICE);
                if (powerManager.isPowerSaveMode()) {
                    Intent battSaverIntent = new Intent();
                    battSaverIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                    startActivityForResult(battSaverIntent, 0);
                } else {
                    recreate();
                }

            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        drawerClosed();
        preferenceHelper.putIsMainScreenVisible(false);
        preferenceHelper.putCheckCountForLocation(countUpdateForLocation);
    }

    @Override
    protected void onDestroy() {
        locationHelper.onStop();
        if (preferenceHelper.getIsProviderOnline() == Const.ProviderStatus.PROVIDER_STATUS_ONLINE && preferenceHelper.getSessionToken() != null) {
            startLocationUpdateService();
        }
//        if(isMyServiceRunning(FloatingViewService.class)){
//            stopService(new Intent(MainDrawerActivity.this,FloatingViewService.class));
//        }
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed() {


        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_EXPANDED) {
            drawerClosed();
        } else {
//            startService(new Intent(MainDrawerActivity.this,FloatingViewService.class));

//
            this.moveTaskToBack(true);
//            super.onBackPressed();
//            openExitDialog(this);
        }
    }

    private void hideControll() {

        View frag = findViewById(R.id.mapFrag);

        ImageView imageView = frag.findViewById(R.id.llCreateTrip);
        FloatingActionButton floatingActionButton = frag.findViewById(R.id.ivTargetLocation);
        toolbar.setVisibility(View.GONE);
        floatingActionButton.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        LinearLayout linearLayout = frag.findViewById(R.id.llVehicleTag);
        linearLayout.setVisibility(View.GONE);

    }

    private void showControll() {

        toolbar.setVisibility(View.VISIBLE);
        View frag = findViewById(R.id.mapFrag);
        FloatingActionButton floatingActionButton = frag.findViewById(R.id.ivTargetLocation);
        floatingActionButton.setVisibility(View.VISIBLE);
        ImageView imageView = frag.findViewById(R.id.llCreateTrip);
        imageView.setVisibility(View.VISIBLE);
        LinearLayout linearLayout = frag.findViewById(R.id.llVehicleTag);
        linearLayout.setVisibility(View.VISIBLE);


    }

    public void goToMapFragment(boolean isFromNotification) {
        if (isFragmentNotAddedEver(Const.Tag.MAP_FRAGMENT)) {
            MapFragment mapFragment = new MapFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(Const.Params.IS_FROM_NOTIFICATION, isFromNotification);
            mapFragment.setArguments(bundle);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in_out, R.anim.slide_out_left, R.anim.fade_in_out, R.anim.slide_out_right);
            ft.replace(R.id.contain_frame, mapFragment, Const.Tag.MAP_FRAGMENT);
            ft.commitNowAllowingStateLoss();
        }
    }

    public void goToTripFragment() {
        if (isFragmentNotAddedEver(Const.Tag.TRIP_FRAGMENT)) {
            startLocationUpdateService();
            TripFragment tripFragment = new TripFragment();
            addFragment(tripFragment, false, true, Const.Tag.TRIP_FRAGMENT);
        }
    }

    public void goToReceiveNewTripFragment(String tripId) {
        if (isFragmentNotAddedEver(Const.Tag.RECEIVE_NEW_TRIP_FRAGMENT)) {
            ReceiveNewTripFragment receiveNewTripFragment = new ReceiveNewTripFragment();
            Bundle bundle = new Bundle();
            bundle.putString("newTrip", tripId);
            receiveNewTripFragment.setArguments(bundle);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in_out, R.anim.slide_out_left, R.anim.fade_in_out, R.anim.slide_out_right);
            ft.replace(R.id.contain_frame, receiveNewTripFragment, Const.Tag.RECEIVE_NEW_TRIP_FRAGMENT);
            ft.commitNowAllowingStateLoss();
        }
    }

    public void goToFeedBackFragment() {
        if (isFragmentNotAddedEver(Const.Tag.FEEDBACK_FRAGMENT)) {
            FeedbackFragment feedbackFragment = new FeedbackFragment();
            addFragment(feedbackFragment, false, true, Const.Tag.FEEDBACK_FRAGMENT);
        }
    }

    public void goToInvoiceFragment() {
        if (isFragmentNotAddedEver(Const.Tag.INVOICE_FRAGMENT)) {
            InvoiceFragment invoiceFragment = new InvoiceFragment();
            addFragment(invoiceFragment, false, true, Const.Tag.INVOICE_FRAGMENT);
        }
    }

    private void gotoBankDetailActivity() {
        Intent bankInfo = new Intent(this, BankDetailActivity.class);
        startActivity(bankInfo);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToSettingsActivity() {
        Intent settings = new Intent(this, SettingActivity.class);
        startActivity(settings);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    private void goToEarningActivity() {
        Intent earning = new Intent(this, EarningActivity.class);
        startActivity(earning);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * this method is start request  service which is used to update provide location and also
     * check is request is occurred
     */
    public void startLocationUpdateService() {
        Intent intent = new Intent(this, EberUpdateService.class);
        intent.setAction(Const.Action.START_FOREGROUND_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void stopLocationUpdateService() {
        stopService(new Intent(this, EberUpdateService.class));
    }

    private void closedPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            customDialogEnable.dismiss();
            customDialogEnable = null;
        }
    }

    private void openPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources().getString(R.string.msg_reason_for_permission_location), getString(R.string.text_i_am_sure), getString(R.string.text_re_try)) {
            @Override
            public void doWithEnable() {
                requestLocationPermission();
                closedPermissionDialog();
            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
                finishAffinity();
            }
        };
        customDialogEnable.show();
    }

    private void openPermissionNotifyDialog(final int code) {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources().getString(R.string.msg_permission_notification), getResources().getString(R.string.text_exit_caps), getResources().getString(R.string.text_settings)) {
            @Override
            public void doWithEnable() {
                closedPermissionDialog();
                startActivityForResult(getIntentForPermission(), code);

            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
                finishAffinity();
            }
        };
        customDialogEnable.show();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (prominentDisclosureDialog != null && prominentDisclosureDialog.isShowing()) {
                return false;
            }
            prominentDisclosureDialog = new LocationProminentDisclosureDialog(this) {
                @Override
                public void next() {
                    prominentDisclosureDialog.dismiss();
                    requestLocationPermission();
                }
            };
            prominentDisclosureDialog.show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Const.PERMISSION_FOR_LOCATION:
                if (checkLocationPermission()) {
                    loadFragmentsAccordingStatus();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            switch (requestCode) {
                case Const.PERMISSION_FOR_LOCATION:
                    goWithLocationPermission(grantResults);
                    break;
                default:
                    break;
            }
        }
    }

    private void goWithLocationPermission(int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Do the stuff that requires permission...
            loadFragmentsAccordingStatus();
            stopLocationUpdateService();
            startLocationUpdateService();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openLocationSettingRecommendDialog();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                openPermissionDialog();
            } else {
                openPermissionNotifyDialog(Const.PERMISSION_FOR_LOCATION);
            }
        }
    }

    public float getBearing(Location begin, Location end) {
        float bearing = 0;
        if (begin != null && end != null) {
            bearing = begin.bearingTo(end);
        }
        return bearing;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (networkListener != null) {
            networkListener.onNetwork(isConnected);
        }
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

    @Override
    public void onAdminApproved() {
        goWithAdminApproved();
    }

    @Override
    public void onAdminDeclined() {
        goWithAdminDecline();
    }

    public void showProgressDialog(String message) {
        if (dialogProgress != null && dialogProgress.isShowing()) {
            return;
        }

        CustomCircularProgressView ivProgressBar;
        MyFontTextView tvTitleProgress;
        dialogProgress = new Dialog(this);
        dialogProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogProgress.setContentView(R.layout.circuler_progerss_bar);
        tvTitleProgress = dialogProgress.findViewById(R.id.tvTitleProgress);
        ivProgressBar = dialogProgress.findViewById(R.id.ivProgressBar);
        ivProgressBar.addListener(new CircularProgressViewAdapter() {
            @Override
            public void onProgressUpdate(float currentProgress) {
                Log.d("CPV", "onProgressUpdate: " + currentProgress);
            }

            @Override
            public void onProgressUpdateEnd(float currentProgress) {
                Log.d("CPV", "onProgressUpdateEnd: " + currentProgress);
            }

            @Override
            public void onAnimationReset() {
                Log.d("CPV", "onAnimationReset");
            }

            @Override
            public void onModeChanged(boolean isIndeterminate) {
                Log.d("CPV", "onModeChanged: " + (isIndeterminate ? "indeterminate" : "determinate"));
            }
        });
        tvTitleProgress.setText(message);
        ivProgressBar.startAnimation();
        dialogProgress.setCancelable(false);
        WindowManager.LayoutParams params = dialogProgress.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogProgress.getWindow().setAttributes(params);
        dialogProgress.getWindow().setDimAmount(0);
        dialogProgress.show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_menu_black_24dp));
    }

    public void closedProgressDialog() {
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }
    }

    public void openUserCancelTripDialog() {
        if (customCancelTripDialog != null && customCancelTripDialog.isShowing()) {
            return;
        }

        customCancelTripDialog = new CustomDialogBigLabel(this, getString(R.string.text_trip_cancelled), getString(R.string.message_trip_cancel), getString(R.string.text_ok), "") {
            @Override
            public void positiveButton() {
                closeUserCancelTripDialog();
            }

            @Override
            public void negativeButton() {

            }
        };
        customCancelTripDialog.show();
    }

    private void closeUserCancelTripDialog() {
        if (customCancelTripDialog != null && customCancelTripDialog.isShowing()) {
            customCancelTripDialog.dismiss();
            customCancelTripDialog = null;
        }
    }

    private void addAppInWhiteList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    private void goToPaymentActivity() {
        Intent paymentIntent = new Intent(MainDrawerActivity.this, PaymentActivity.class);
        startActivity(paymentIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * this method filter fluctuate location
     *
     * @param location
     */
    public void locationFilter(Location location) {
        if (location == null) return;
        if (currentLocation == null) {
            currentLocation = location;
        }
        kalmanLatLong.Process(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getTime());
        currentLocation.setLatitude(kalmanLatLong.get_lat());
        currentLocation.setLongitude(kalmanLatLong.get_lng());
        currentLocation.setAccuracy(kalmanLatLong.get_accuracy());
    }

    public void drawerClosed() {
        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_EXPANDED) {
            topSheetBehavior.setState(TopSheetBehavior.STATE_COLLAPSED);
        }
    }

    public void drawerOpen() {
        if (topSheetBehavior.getState() == TopSheetBehavior.STATE_COLLAPSED) {
            topSheetBehavior.setState(TopSheetBehavior.STATE_EXPANDED);
        }
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, boolean isAnimate, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (isAnimate) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        }
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.contain_frame, fragment, tag);
        ft.commitNowAllowingStateLoss();
    }

    public void setLastLocation(Location location) {
        if (location != null) {
            if (lastLocation == null) {
                lastLocation = new Location("lastLocation");
            }
            lastLocation.set(location);
        }
    }

    private boolean isFragmentNotAddedEver(String fragmentTag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        return fragment == null || !fragment.isAdded();
    }

    private void initStripePayment() {
        final PaymentAuthConfig.Stripe3ds2UiCustomization uiCustomization = new PaymentAuthConfig.Stripe3ds2UiCustomization.Builder().build();
        PaymentAuthConfig.init(new PaymentAuthConfig.Builder().set3ds2Config(new PaymentAuthConfig.Stripe3ds2Config.Builder()
                // set a 5 minute timeout for challenge flow
                .setTimeout(5)
                // customize the UI of the challenge flow
                .setUiCustomization(uiCustomization).build()).build());

        PaymentConfiguration.init(this, preferenceHelper.getStripePublicKey());
        stripe = new Stripe(this, PaymentConfiguration.getInstance(this).getPublishableKey());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragmentList) {
            if (fragment instanceof MapFragment) {
                if (fragment.isAdded() && fragment.isInLayout()) {
                    MapFragment mapFragment = (MapFragment) fragment;
                    mapFragment.getTrips();
                    break;
                }
            }
        }
    }

    protected void openLocationSettingRecommendDialog() {
        if (locationSettingRecommendDialog != null && locationSettingRecommendDialog.isShowing()) {
            return;
        }

        locationSettingRecommendDialog = new CustomDialogBigLabel(this, getString(R.string.text_update_location_settings), getString(R.string.msg_update_location_settings), getString(R.string.text_update_settings), getString(R.string.text_no_thanks)) {
            @Override
            public void positiveButton() {
                locationSettingRecommendDialog.dismiss();
                startActivityForResult(getIntentForPermission(), Const.PERMISSION_FOR_LOCATION);
            }

            @Override
            public void negativeButton() {
                locationSettingRecommendDialog.dismiss();
                loadFragmentsAccordingStatus();
                stopLocationUpdateService();
                startLocationUpdateService();
            }
        };
        locationSettingRecommendDialog.show();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainDrawerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Const.PERMISSION_FOR_LOCATION);
    }

    /**
     * Interface which help to get current location in fragment
     */

    public interface LocationReceivedListener {
        void onLocationReceived(Location location);

    }

    public interface NetworkListener {
        void onNetwork(boolean isConnected);
    }

}