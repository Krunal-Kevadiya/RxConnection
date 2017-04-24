package com.kevadiyakrunalk.rxconnection;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import rx.Observable;
import rx.functions.Func1;

public class RxNetwork {
    private RxNetwork() {
        // No instances
    }

    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return null != activeNetwork && activeNetwork.isConnected();
    }

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ninfo = cm.getActiveNetworkInfo();
            if (ninfo != null && ninfo.isConnected()) {
                if (ninfo.getTypeName().equalsIgnoreCase("WIFI")) {
                    return isWifiOnline();
                } else
                    return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isWifiOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal == 0);
            if (reachable)
                return reachable;

        } catch (Exception ignored) {
        }
        return false;
    }

    public static Observable<Boolean> stream(Context context) {
        final Context applicationContext = context.getApplicationContext();
        final IntentFilter action = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        return ContentObservable.fromBroadcast(context, action)
                .startWith((Intent) null)
                .map(new Func1<Intent, Boolean>() {
                    @Override
                    public Boolean call(Intent ignored) {
                        //return getConnectivityStatus(applicationContext);
                        return isOnline(applicationContext);
                    }
                }).distinctUntilChanged();
    }
}