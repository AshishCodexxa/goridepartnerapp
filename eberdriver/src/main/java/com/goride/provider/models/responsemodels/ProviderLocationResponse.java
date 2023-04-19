package com.goride.provider.models.responsemodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ProviderLocationResponse implements Parcelable {

    @SerializedName("location_unique_id")
    private int locationUniqueId;

    @SerializedName("total_distance")
    private double totalDistance;

    @SerializedName("total_time")
    private int totalTime;

    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("error_code")
    private int errorCode;

    @SerializedName("driver_inside_zone")
    private boolean driverInsideZone;

    @SerializedName("driver_que_number")
    private int driverQueNumber;

    protected ProviderLocationResponse(Parcel in) {
        locationUniqueId = in.readInt();
        totalDistance = in.readDouble();
        totalTime = in.readInt();
        success = in.readByte() != 0;
        message = in.readString();
        errorCode = in.readInt();
        driverInsideZone = in.readByte() != 0;
        driverQueNumber = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(locationUniqueId);
        dest.writeDouble(totalDistance);
        dest.writeInt(totalTime);
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeString(message);
        dest.writeInt(errorCode);
        dest.writeByte((byte) (driverInsideZone ? 1 : 0));
        dest.writeInt(driverQueNumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProviderLocationResponse> CREATOR = new Creator<ProviderLocationResponse>() {
        @Override
        public ProviderLocationResponse createFromParcel(Parcel in) {
            return new ProviderLocationResponse(in);
        }

        @Override
        public ProviderLocationResponse[] newArray(int size) {
            return new ProviderLocationResponse[size];
        }
    };

    public int getLocationUniqueId() {
        return locationUniqueId;
    }

    public void setLocationUniqueId(int locationUniqueId) {
        this.locationUniqueId = locationUniqueId;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
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

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isDriverInsideZone() {
        return driverInsideZone;
    }

    public void setDriverInsideZone(boolean driverInsideZone) {
        this.driverInsideZone = driverInsideZone;
    }

    public int getDriverQueNumber() {
        return driverQueNumber;
    }

    public void setDriverQueNumber(int driverQueNumber) {
        this.driverQueNumber = driverQueNumber;
    }
}
