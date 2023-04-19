package com.goride.provider.models.responsemodels;

import com.goride.provider.models.datamodels.AdminSettings;
import com.goride.provider.models.datamodels.ProviderData;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SettingsDetailsResponse {

    @SerializedName("trip_detail")
    private List<String> tripsResponse;
    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("setting_detail")
    private AdminSettings adminSettings;
    @SerializedName("provider_detail")
    private ProviderData providerData;
    @SerializedName("near_trip_detail")
    private TripsResponse tripsNewResponse;

    public List<String> getTripsResponse() {
        return tripsResponse;
    }

    public void setTripsResponse(List<String> tripsResponse) {
        this.tripsResponse = tripsResponse;
    }

    public ProviderData getProviderData() {
        return providerData;
    }

    public void setProviderData(ProviderData providerData) {
        this.providerData = providerData;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AdminSettings getAdminSettings() {
        return adminSettings;
    }

    public void setAdminSettings(AdminSettings adminSettings) {
        this.adminSettings = adminSettings;
    }

    public void setTripsNewResponse(TripsResponse tripsNewResponse) {
        this.tripsNewResponse = tripsNewResponse;
    }

    public TripsResponse getTripsNewResponse() {
        return tripsNewResponse;
    }
}
