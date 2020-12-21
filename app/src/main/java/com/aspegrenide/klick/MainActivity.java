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
import android.content.res.Configuration;
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

public class MainActivity extends AppCompatActivity {
    public static final String CARDNOTFOUND = "CARDNOTFOUND";
    public static final String CARD_DETAILS = "carddetails";
    private static final String TAG = "MAIN BLE";
    private static final String KLICK_CONNECTED = "KLICK_CONNECTED";

    private boolean klickConnected = false;

    // Bluetooth's variables
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothManager bluetoothManager;
    BluetoothScanCallback bluetoothScanCallback;
    BluetoothGatt gattClient;
    BluetoothGattCharacteristic characteristicID; // To get Value


    // funkar, lyckas läsa BLEuart från Adafruit
    final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    final UUID CHARACTERISTIC_UUID_ID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    final UUID DESCRIPTOR_UUID_ID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"); //0x2902

    TextView tvConnectionStateLand;
    TextView tvConnectionStatePort;
    TextView tvPlacePermission;
    TextView tvOnTopPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_red);

        tvConnectionStateLand = (TextView) findViewById(R.id.tvConnectionStateLand);
        tvConnectionStatePort = (TextView) findViewById(R.id.tvConnectionStatePort);
        tvPlacePermission = (TextView) findViewById(R.id.tvPlacePermission);
        tvOnTopPermission = (TextView) findViewById(R.id.tvOnTopPermission);

        String failedCardId;
        Bundle extras = getIntent().getExtras();

        Intent intent = getIntent();
        if (null != intent) { //Null Checking
            klickConnected = intent.getBooleanExtra(KLICK_CONNECTED, false);
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
        updateScreen();

        // get datahandler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, DataHandlerService.class));
        } else {
            startService(new Intent(this, DataHandlerService.class));
        }

        // check permission place
        if ((ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) ||
        (!Settings.canDrawOverlays(this))) {
            tvPlacePermission.setText("Klick måste ställas in först!");
            tvOnTopPermission.setText("Se mer under 'Hur ställer jag in...' ");
        }
        // Bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        startScan();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KLICK_CONNECTED, klickConnected);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    private void updateScreen() {
        int display_mode = getResources().getConfiguration().orientation;
        if (display_mode == Configuration.ORIENTATION_PORTRAIT) {
            if (!klickConnected) {
                tvConnectionStatePort.setText("Väntar på Klick...");
            }else {
                tvConnectionStatePort.setText("Kopplad till Klick");
            }
        } else {
            if (!klickConnected) {
                tvConnectionStateLand.setText("Väntar på Klick...");
            }else {
                tvConnectionStateLand.setText("Kopplad till Klick");
            }
        }
    }

    public void onClickOnTopPermission(View view) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 0);
    }

    public void onClickPlacePermission(View view) {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + this.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        this.startActivity(i);
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
        Log.d(TAG, "state " + klickConnected);
        updateScreen();
/*
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bdl = new Bundle();
        bdl.putBoolean(KLICK_CONNECTED, klickConnected);
        intent.putExtras(bdl);
        startActivity(intent);

 */
    }


    private CardDetails lookUpCardDetails(String cardUid) {
        ArrayList<CardDetails> cardDetailsList = DataHandlerService.getCards();
//        ArrayList<CardDetails> cardDetailsList = MainActivity.getCards();
        for (CardDetails cd: cardDetailsList) {
            Log.d(TAG, "getCardId()" + cd.getCardId());
            Log.d(TAG, "getCardId()" + cardUid);
            if(cd.getCardId().equals(cardUid)) {
                Log.d(TAG, "Found match " + cd.getUri() + " id " + cd.getName());
                return (cd);
            }
        }
        return null;
    }

    // ACTION
    // this is called when a characteristic is changed
    private void callAppstarter(String cardUid) {
        CardDetails cd = lookUpCardDetails(cardUid);
        if (cd == null) {
            Log.d(TAG, "NO MATCH on cardUid: " + cardUid);
            Toast.makeText(this, "Kände inte igen kortet" + cardUid, Toast.LENGTH_SHORT).show();
            // need to bring me to the front
//            this.finish();
        }

        Log.d("USB Listener", "cardUid" + cardUid);
        Intent startAppIntent = new Intent(this, AppstarterService.class);
        startAppIntent.putExtra("CARDUID", cardUid);
        startService(startAppIntent);

    }

    // BLUETOOTH SCAN
    public void btnScanOnClick(View view) {
        startScan();
    }

    private void startScan(){
        Log.i(TAG,"startScan()");
        bluetoothScanCallback = new BluetoothScanCallback();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(bluetoothScanCallback);
    }

    // BLUETOOTH CONNECTION
    private void connectDevice(BluetoothDevice device) {
        if (device == null) Log.i(TAG,"Device is null");
        GattClientCallback gattClientCallback = new GattClientCallback();
        // set autoconnect to true, in case Klick resets
        gattClient = device.connectGatt(this,true,gattClientCallback);
    }

    public void onClickOpenSettingsBtn(View view) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    // BLE Scan Callbacks
    private class BluetoothScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i(TAG, "onScanResult, result = " + result);
//            Log.i(TAG, "gattClient,  = " + gattClient);
            if (result.getDevice().getName() != null){
                if (result.getDevice().getName().equals("Klick")) {
                    Log.i(TAG, "KLICK CONNECTED");
                    // When find your device, connect.
                    connectDevice(result.getDevice());
                    bluetoothLeScanner.stopScan(bluetoothScanCallback); // stop scan
                    klickConnected = true;
                    updateScreen();
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i(TAG, "onBathScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(TAG, "ErrorCode: " + errorCode);
        }
    }

    // Bluetooth GATT Client Callback
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG,"onConnectionStateChange");

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.i(TAG, "onConnectionStateChange GATT FAILURE");
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onConnectionStateChange != GATT_SUCCESS");
                klickConnected = false;
                updateScreen();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "onConnectionStateChange CONNECTED");
                klickConnected = true;
                updateScreen();
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "onConnectionStateChange DISCONNECTED");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG,"onServicesDiscovered");
            if (status != BluetoothGatt.GATT_SUCCESS) return;

            // Reference your UUIDs
            characteristicID = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID_ID);
            gatt.setCharacteristicNotification(characteristicID,true);

            BluetoothGattDescriptor descriptor = characteristicID.getDescriptor(DESCRIPTOR_UUID_ID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG,"onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG,"onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG,"onCharacteristicChanged");

            // Here you can read the characteristc's value
            final String cardUid = new String(characteristic.getValue());
            Log.i(TAG,"cardUid" + cardUid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                }


            });

            // MAGIC val{66633248}
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(TAG,"onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG,"onDescriptorWrite");
        }
    }


}