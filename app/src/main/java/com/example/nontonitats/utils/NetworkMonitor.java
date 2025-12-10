package com.example.nontonitats.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class NetworkMonitor {

    public interface NetworkListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context, NetworkListener listener) {

        connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                listener.onNetworkAvailable();
            }

            @Override
            public void onLost(Network network) {
                listener.onNetworkLost();
            }
        };
    }

    public void startMonitor() {
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    public void stopMonitor() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

}
