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

public class MainActivity extends AppCompatActivity {
    public static final String CARDNOTFOUND = "CARDNOTFOUND";
    public static final String CARD_DETAILS = "carddetails";
    private static final String TAG = "MAIN BLE";
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

    TextView tvConnectionState;
    TextView tvPlacePermission;
    TextView tvOnTopPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_red);

        tvConnectionState = (TextView) findViewById(R.id.tvConnectionState);
        tvPlacePermission = (TextView) findViewById(R.id.tvPlacePermission);
        tvOnTopPermission = (TextView) findViewById(R.id.tvOnTopPermission);

        String failedCardId;
        Bundle extras = getIntent().getExtras();

        // get datahandler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, DataHandlerService.class));
        } else {
            startService(new Intent(this, DataHandlerService.class));
        }

        // check permission place
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Klick behöver behörighet till 'Plats'", Toast.LENGTH_LONG).show();
            tvPlacePermission.setText("Klick behöver behörighet till 'Plats', klicka HÄR för att fixa");
        }

        // check permission OnTop
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Klick behöver behörighet att 'Visas Överst'", Toast.LENGTH_LONG).show();
            tvOnTopPermission.setText("Klick behöver behörighet att 'Visas Överst', klicka HÄR för att fixa");
        }
        // Bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        startScan();
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
    }
    public void onClickContactBtn(View view) {
    }
    public void onClickAboutBtn(View view) {
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



    // BLE Scan Callbacks
    private class BluetoothScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.i(TAG, "onScanResult");
            if (result.getDevice().getName() != null){
                if (result.getDevice().getName().equals("Klick")) {
                    tvConnectionState.setText("Klick är kopplad");
                    Log.i(TAG, "KLICK CONNECTED");
                    // When find your device, connect.
                    connectDevice(result.getDevice());
                    bluetoothLeScanner.stopScan(bluetoothScanCallback); // stop scan
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
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "onConnectionStateChange CONNECTED");
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