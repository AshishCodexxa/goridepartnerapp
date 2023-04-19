package com.goride.provider.models.responsemodels;

import com.goride.provider.models.datamodels.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TripsResponse {

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("destination_address")
    private String destinationAddress;

    @SerializedName("is_trip_end")
    private int isTripEnd;

    @SerializedName("success")
    private boolean success;

    @SerializedName("time_left_to_responds_trip")
    private int timeLeftToRespondsTrip;

    @SerializedName("source_address")
    private String sourceAddress;

    @SerializedName("destinationLocation")
    private List<Double> destinationLocation;

    @SerializedName("sourceLocation")
    private List<Double> sourceLocation;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("unique_id")
    private int uniqueId;


    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getIsTripEnd() {
        return isTripEnd;
    }

    public void setIsTripEnd(int isTripEnd) {
        this.isTripEnd = isTripEnd;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTimeLeftToRespondsTrip() {
        return timeLeftToRespondsTrip;
    }

    public void setTimeLeftToRespondsTrip(int timeLeftToRespondsTrip) {
        this.timeLeftToRespondsTrip = timeLeftToRespondsTrip;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public List<Double> getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(List<Double> destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public List<Double> getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(List<Double> sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return "TripsResponse{" +
                "tripId='" + tripId + '\'' +
                ", destinationAddress='" + destinationAddress + '\'' +
                ", isTripEnd=" + isTripEnd +
                ", success=" + success +
                ", timeLeftToRespondsTrip=" + timeLeftToRespondsTrip +
                ", sourceAddress='" + sourceAddress + '\'' +
                ", destinationLocation=" + destinationLocation +
                ", sourceLocation=" + sourceLocation +
                ", message='" + message + '\'' +
                ", user=" + user +
                ", uniqueId=" + uniqueId +
                '}';
    }
}