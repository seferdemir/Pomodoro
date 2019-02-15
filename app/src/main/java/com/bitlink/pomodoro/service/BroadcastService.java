package com.bitlink.pomodoro.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class BroadcastService extends Service {

    private final static String TAG = BroadcastService.class.getSimpleName();

    public static final String COUNTDOWN_BR = BroadcastService.class.getSimpleName();

    private SharedPreferences mSPreferences;

    private CountDownTimer countDownTimer = null;

    public static final long NOTIFY_INTERVAL = 1000;

    Intent intent = new Intent(COUNTDOWN_BR);

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Starting timer...");

        mSPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long total_milis = mSPreferences.getLong("millisInFuture", 0);

        countDownTimer = new CountDownTimer(total_milis, NOTIFY_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);

                intent.putExtra("countdown", millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer finished");

                intent.putExtra("finish", true);
                sendBroadcast(intent);
            }
        };

        countDownTimer.start();
    }

    @Override
    public void onDestroy() {

        countDownTimer.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public class MyBinder extends Binder {
        public BroadcastService getService() {
            return BroadcastService.this;
        }
    }
}