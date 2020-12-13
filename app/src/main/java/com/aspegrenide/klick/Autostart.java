package com.aspegrenide.klick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

 /*
    This class is called on BOOT_COMPLETE
    The idea is to make sure that BlueToothConnectionService is
    running and prepared to handle incoming cardUids from the
    RFID reader case
 */

public class Autostart extends BroadcastReceiver {
    private static final String LOG_TAG = "KLICK Autostart";
    private static ArrayList<CardDetails> cardDetailsList = new ArrayList<CardDetails>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "device rebooted");
        Intent startMe = new Intent(context, MainActivity.class);
        context.startActivity(startMe);
    }
}
