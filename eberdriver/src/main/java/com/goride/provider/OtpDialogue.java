package com.goride.provider;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public abstract class OtpDialogue extends Dialog implements View.OnClickListener {

    private final EditText edOtpText;
    private final Button btnTripCancel;
    private final Context context;

    public OtpDialogue(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.verify_otp);
        this.context = context;
//        ivDriverPhotoDialog = findViewById(R.id.ivDriverPhotoDialog);
        btnTripCancel = findViewById(R.id.btn_start_ride);
        edOtpText = findViewById(R.id.ed_otp);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        getWindow().setDimAmount(0);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.setCancelable(false);



    }


    @Override
    public void onClick(View v) {
        doWithOk();
    }

    public abstract void doWithOk();
}

