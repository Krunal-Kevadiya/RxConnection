package com.kevadiyakrunalk.rxconnection;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StatusView extends RelativeLayout {
    private static final int DISMISS_ON_COMPLETE_DELAY = 1000;

    private Status currentStatus;

    private View loadingview;
    private TextView timerView;
    private TextView retryView;

    private int mStartCount;
    private int mCurrentCount;
    private int mSavedCount;

    private Handler handler;
    private TimerListener timerListener;
    private Handler mHandler = new Handler();

    private Runnable autoDismissOnComplete = new Runnable() {
        @Override
        public void run() {
            View view = getCurrentView(currentStatus);
            if (view != null)
                view.setVisibility(INVISIBLE);
            handler.removeCallbacks(autoDismissOnComplete);
            timerListener.onConnectionStatus(true);
        }
    };

    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentCount > 0) {
                if(timerView != null)
                    timerView.setText(mCurrentCount + "");
                mCurrentCount--;
            } else {
                timerListener.OnTimeChanged(true);
            }
        }
    };

    public void start() {
        mHandler.removeCallbacks(mCountDownRunnable);

        if(timerView != null) {
            timerView.setText(mStartCount + "");
            timerView.setVisibility(View.VISIBLE);
        }

        mCurrentCount = mStartCount;
        mSavedCount = mStartCount;

        mHandler.post(mCountDownRunnable);
        for (int i = 1; i <= mStartCount; i++) {
            mHandler.postDelayed(mCountDownRunnable, i * 1000);
        }
    }

    public void continueTimer() {
        mHandler.removeCallbacks(mCountDownRunnable);
        if(timerView != null) {
            timerView.setText(mSavedCount + "");
            timerView.setVisibility(VISIBLE);
        }
        mCurrentCount = mSavedCount * 2;
        mSavedCount = mCurrentCount;
        mHandler.post(mCountDownRunnable);
        for (int i = 1; i <= mSavedCount; i++) {
            mHandler.postDelayed(mCountDownRunnable, i * 1000);
        }
    }

    public void cancel() {
        mHandler.removeCallbacks(mCountDownRunnable);
        if(timerView != null) {
            timerView.setText("");
            timerView.setVisibility(View.GONE);
        }
    }

    public void setStartCount(int startCount) {
        this.mStartCount = startCount;
    }

    public int getStartCount() {
        return mStartCount;
    }

    public StatusView(Context context) {
        super(context);
        init(context, null, R.layout.sv_layout_loading);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.layout.sv_layout_loading);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public StatusView(Context context, int loadingLayout) {
        super(context);
        init(context, null, loadingLayout);
    }

    public StatusView(Context context, AttributeSet attrs, int loadingLayout) {
        super(context, attrs);
        init(context, attrs, loadingLayout);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int loadingLayout, boolean... flag) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, loadingLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int loadingLayout) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, loadingLayout);
    }

    private void init(Context context, AttributeSet attrs, int loadingLayout) {
        currentStatus = Status.IDLE;
        LayoutInflater inflater = LayoutInflater.from(context);
        handler = new Handler();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.statusview);

        int loadingLayoutId = a.getResourceId(R.styleable.statusview_loading, 0);
        boolean isListener = a.getBoolean(R.styleable.statusview_only_listener, false);
        int retryId = a.getResourceId(R.styleable.statusview_retry, 0);
        int timerId = a.getResourceId(R.styleable.statusview_timer, 0);

        if(!isListener) {
            if (loadingLayoutId != 0) {
                loadingview = inflater.inflate(loadingLayoutId, null);
                if (retryId != 0)
                    retryView = (TextView) loadingview.findViewById(retryId);
                if (timerId != 0)
                    timerView = (TextView) loadingview.findViewById(timerId);
            } else {
                if (loadingLayout != 0)
                    loadingview = inflater.inflate(loadingLayout, null);
                else
                    loadingview = inflater.inflate(R.layout.sv_layout_loading, null);

                retryView = (TextView) loadingview.findViewById(R.id.retryText);
                timerView = (TextView) loadingview.findViewById(R.id.timerView);
            }
            loadingview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            addView(loadingview);
            loadingview.setVisibility(View.INVISIBLE);
        }

        if (retryView != null)
            retryView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    timerListener.onConnectionRetry();
                }
            });
        a.recycle();
    }

    public void setOnTimeChangeListener(TimerListener timeChangeListener) {
        timerListener = timeChangeListener;
    }

    public View getLoadingView() {
        return loadingview;
    }

    public void setStatus(final Status status) {
        if (currentStatus == Status.IDLE) {
            currentStatus = status;
            View view = getCurrentView(currentStatus);
            if (view != null)
                view.setVisibility(VISIBLE);
        } else if (status != Status.IDLE) {
            View exitView = getCurrentView(currentStatus);
            if (exitView != null)
                exitView.setVisibility(View.INVISIBLE);
            View enterView = getCurrentView(status);
            if (enterView != null)
                enterView.setVisibility(View.VISIBLE);
            currentStatus = status;
        } else {
            View view = getCurrentView(currentStatus);
            if (view != null)
                view.setVisibility(INVISIBLE);
        }

        handler.removeCallbacksAndMessages(null);
        if (status == Status.COMPLETE)
            handler.postDelayed(autoDismissOnComplete, DISMISS_ON_COMPLETE_DELAY);
        else if (status == Status.LOADING)
            timerListener.onConnectionStatus(false);
    }

    private View getCurrentView(Status status) {
        if (status == Status.IDLE)
            return null;
        else if (status == Status.LOADING)
            return loadingview;
        return null;
    }

    public interface TimerListener {
        void onConnectionRetry();

        void OnTimeChanged(boolean isFinished);

        void onConnectionStatus(boolean isConnected);
    }
}
