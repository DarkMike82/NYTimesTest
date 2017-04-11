package com.trivago.nytimestest.core;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;

import com.trivago.nytimestest.R;

/**
 * Created by Michael Dontsov on 09.04.2017.
 */

public class PopupController {
    private static ProgressDialog sProgress;
    private static AlertDialog sAlert;

    public static void showProgress(Context context, @StringRes int msgId) {
        if (sProgress != null && sProgress.isShowing())
            return;
        CharSequence msg = context.getString(msgId);
        showProgress(context, msg);
    }

    public static void showProgress(Context context, CharSequence msg) {
        if (sProgress != null && sProgress.isShowing())
            return;
        sProgress = ProgressDialog.show(context, null, msg);
    }

    public static void hideProgress(){
        if (sProgress != null)
            sProgress.dismiss();
    }

    public static void showMessage(Context context, @StringRes int msgId) {
        String msg = context.getResources().getString(msgId);
        showMessage(context, msg);
    }

    public static void showMessage(Context context, String msg) {
        if (sAlert != null)
            sAlert.dismiss();
        sAlert = new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sAlert.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
        sAlert.show();
    }
}
