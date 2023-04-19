package com.goride.provider.models.responsemodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetTripsDetailResponse {

    @SerializedName("success")
    private Boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("trip_detail")
    private List<TripsResponse> tripDetail = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TripsResponse> getTripDetail() {
        return tripDetail;
    }

    public void setTripDetail(List<TripsResponse> tripDetail) {
        this.tripDetail = tripDetail;
    }
}
