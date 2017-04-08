package com.trivago.nytimestest.core;

import android.app.Application;
import android.os.Handler;

/**
 * Created by Michael Dontsov on 08.04.2017.
 */

public class NytApp extends Application {

    public static NytApp instance;
    private static Handler uiHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        uiHandler = new Handler(this.getMainLooper());
    }

    @Override
    public void onTerminate() {
        instance = null;
        uiHandler = null;
        super.onTerminate();
    }

    public static void RunOnUiThread(Runnable r) {
        if (uiHandler == null)
            return;
        uiHandler.post(r);
    }
}
