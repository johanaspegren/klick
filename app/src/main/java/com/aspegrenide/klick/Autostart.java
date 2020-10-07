package com.aspegrenide.klick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             context.startForegroundService(new Intent(context, BluetoothConnectionService.class));
             context.startForegroundService(new Intent(context, DataHandlerService.class));
         } else {
             context.startService(new Intent(context, BluetoothConnectionService.class));
             context.startService(new Intent(context, DataHandlerService.class));
         }
     }
 }
