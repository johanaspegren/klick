package com.aspegrenide.klick;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContactActivity extends AppCompatActivity {
    private static final String TAG = "MAIN BLE";
    private static final String KLICK_CONNECTED = "KLICK_CONNECTED";

    private boolean klickConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_contact);

        Intent intent = getIntent();
        if (null != intent) { //Null Checking
            klickConnected = getIntent().getExtras().getBoolean(KLICK_CONNECTED);
        }
        // check if there is a saved state (for example orientation)
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            klickConnected = savedInstanceState.getBoolean(KLICK_CONNECTED);
        } else {
            // Probably initialize members with default values for a new instance
            klickConnected = false;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KLICK_CONNECTED, klickConnected);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onClickConfigureBtn(View view) {
        setContentView(R.layout.activity_main_setup);
/*
        Intent intent = new Intent(this, SetupActivity.class);
        Bundle bdl = new Bundle();
        bdl.putBoolean(KLICK_CONNECTED, klickConnected);
        intent.putExtras(bdl);
        startActivity(intent);

 */
    }

    public void onClickContactBtn(View view) {
        setContentView(R.layout.activity_main_contact);
/*
        Intent intent = new Intent(this, ContactActivity.class);
        Bundle bdl = new Bundle();
        bdl.putBoolean(KLICK_CONNECTED, klickConnected);
        intent.putExtras(bdl);
        startActivity(intent);

 */
    }

    public void onClickAboutBtn(View view) {
        setContentView(R.layout.activity_main_red);
/*
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bdl = new Bundle();
        bdl.putBoolean(KLICK_CONNECTED, klickConnected);
        intent.putExtras(bdl);
        startActivity(intent);

 */
    }

}