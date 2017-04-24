package com.kevadiyakrunalk.rxconnection;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import rx.Observable;

public class ContentObservable {
    private ContentObservable() {
        throw new AssertionError("No Instance");
    }

    public static Observable<Intent> fromBroadcast(Context context, IntentFilter intentFilter) {
        return Observable.create(new OnSubscribeBroadcastRegister(context, intentFilter, null, null));
    }
}
