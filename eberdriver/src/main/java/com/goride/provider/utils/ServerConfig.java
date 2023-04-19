package com.goride.provider.utils;

import android.content.Context;

import com.goride.provider.BuildConfig;
import com.goride.provider.parse.ApiClient;

public class ServerConfig {

    public static String BASE_URL = BuildConfig.BASE_URL;

    public static void setURL(Context context) {
        BASE_URL = PreferenceHelper.getInstance(context).getBaseUrl();
        new ApiClient().changeAllApiBaseUrl(BASE_URL);
    }
}