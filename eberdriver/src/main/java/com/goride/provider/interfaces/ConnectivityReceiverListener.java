package com.goride.provider.interfaces;

public interface ConnectivityReceiverListener {
    void onNetworkConnectionChanged(boolean isConnected);

    void onGpsConnectionChanged(boolean isConnected);
}

