package com.goride.provider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.goride.provider.components.MyFontEdittextView;
import com.goride.provider.components.MyFontTextView;
import com.goride.provider.utils.Utils;


public class ContactUsActivity extends BaseAppCompatActivity {

    private MyFontEdittextView etAdminEmail, etAdminPhoneNo;
    private MyFontTextView tvThankYouFor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.text_contact_us));
        etAdminEmail = findViewById(R.id.etAdminEmail);
        tvThankYouFor = findViewById(R.id.tvThankYouFor);
        etAdminPhoneNo = findViewById(R.id.etAdminPhoneNo);
        tvThankYouFor.setText(getString(R.string.text_thank_you_for_choosing, getString(R.string.app_name)));
        etAdminEmail.setOnClickListener(this);
        etAdminPhoneNo.setOnClickListener(this);
        setProfileData();
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etAdminEmail:
                sendEmail(preferenceHelper.getContactUsEmail());
                break;
            case R.id.etAdminPhoneNo:
                Utils.openCallChooser(ContactUsActivity.this, preferenceHelper.getAdminPhone());
                break;
            default:
                break;
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

    private void setProfileData() {
        etAdminEmail.setText(preferenceHelper.getContactUsEmail());
        etAdminPhoneNo.setText(preferenceHelper.getAdminPhone());
    }

    private void sendEmail(String mailTo) {
        if (!TextUtils.isEmpty(mailTo)) {
            mailTo = "mailto:" + mailTo;
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailTo));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Utils.showToast(getString(R.string.text_no_email_app), this);
            }
        }
    }
}
