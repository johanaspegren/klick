package com.aspegrenide.klick;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class ListenerService extends Service implements ArduinoListener {

    private Arduino arduino;
    private String cardUid;
    private ArrayList<CardDetails> cardDetailsList;

    public ListenerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("USB Listener", "INIT");
        arduino = new Arduino(this);
        arduino.setArduinoListener(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_SHORT).show();
        if (arduino != null) {
            arduino.unsetArduinoListener();
            arduino.close();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        Toast.makeText(this, "Arduino attached", Toast.LENGTH_SHORT).show();
        arduino.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Toast.makeText(this, "Arduino detached", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        String msgFromArduino = new String(bytes);
        startActivity(msgFromArduino);
    }

    @Override
    public void onArduinoOpened() {
    }
    private void startActivity(String msgFromArduino) {
        // unfortunately data comesin burst, wait for all 8 in the string until launch
        Log.d("USB Listener", "incoming " + msgFromArduino);
        cardUid += msgFromArduino;
        String useMe = "";
        if (cardUid.contains("{") && cardUid.contains("}")) {
            // go
            useMe = cardUid.substring((cardUid.indexOf("{") + 1), cardUid.indexOf("}"));
        } else {
            return;
        }
        // 0x04476e7aaa4a80
        Log.d("USB Listener", "START with " + useMe);
        callAppstarter(useMe);
        cardUid = "";

    }

    private void callAppstarter(String cardUid) {
        Log.d("USB Listener", "cardUid" + cardUid);
        Intent startAppIntent = new Intent(this, AppstarterService.class);
        startAppIntent.putExtra("CARDUID", cardUid);
        startService(startAppIntent);
    }


}
