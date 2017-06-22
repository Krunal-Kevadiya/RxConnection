package com.kevadiyakrunalk.rxconnection;

import android.content.Context;

import java.lang.ref.WeakReference;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class ConnectionManager {
    private WeakReference<Context> context;
    private WeakReference<StatusView> statusView;
    public boolean hasNetwork;

    private ConnectionManager(Context context, StatusView statusView) {
        this.context = new WeakReference<>(context);
        this.statusView = new WeakReference<>(statusView);
        initRxNetwork();
    }

    public static class Builder {
        private Context context;
        private StatusView statusView;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setStatusView(StatusView statusView) {
            this.statusView = statusView;
            return this;
        }

        public ConnectionManager build() {
            return new ConnectionManager(context, statusView);
        }
    }

    public void initRxNetwork() {
        if(context != null && context.get() != null) {
            RxNetwork.stream(context.get())
                    .map(new Func1<Boolean, Boolean>() {
                        @Override
                        public Boolean call(Boolean hasInternet) {
                            hasNetwork = hasInternet;
                            if (!hasInternet) {
                                return hasInternet;
                            }
                            return true;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                                   @Override
                                   public void call(Boolean isOnline) {
                                       if(statusView != null && statusView.get() != null) {
                                           statusView.get().setStatus(isOnline ? Status.COMPLETE : Status.LOADING);
                                           if (!isOnline) {
                                               statusView.get().setStartCount(2);
                                               statusView.get().start();
                                           }
                                       }
                                   }
                               }
                    );
        }
    }
}
