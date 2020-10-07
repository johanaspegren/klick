package com.aspegrenide.klick;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "KLICK Main";
        /*
         Very tricky, BOOT_COMPLETE triggar inte, visar sig att den är blockad
         W/BroadcastQueue: Reject to launch app com.aspegrenide.klick/10100 for broadcast: App Op 69
          Asus har en Auto-start manager under Power Performance där appar är blockade från att
          starta på boot, slå på
         */

        /*
         Started by USB attach, fetches the cardDetails from FireBase (done in service later on)
         buttons to test the AppstarterService using intents

        adb C:\Users\johan\AppData\Local\Android\Sdk\platform-tools
        adb tcpip 5555
        adb connect 192.168.1.22:5555

        You will find the ip in Settings/About/Device status/Status
         */

        // TODO: läs in databasen i service eller liknande så att den är tillgänglig
        // också när vi startar som service efter BOOT

    private static ArrayList<CardDetails> cardDetailsList = new ArrayList<CardDetails>();

    CardsRecyclerViewAdapter cardsRecyclerViewAdapter;
    TextView unregisteredCardUid;
    private String BLUETOOTH_MESSAGE_CONTENT = "BLUETOOTH_MESSAGE_CONTENT";

    Intent mServiceIntent;
    private BluetoothConnectionService mBluetoothConnectionService;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "INIT");
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardsRecyclerViewAdapter = new CardsRecyclerViewAdapter(this, cardDetailsList);
        recyclerView.setAdapter(cardsRecyclerViewAdapter);

        unregisteredCardUid = (TextView) findViewById(R.id.tvUnregisteredCardId);

        Intent registerCard = getIntent();
        String cardUid = registerCard.getStringExtra("REGISTER");
        if (cardUid != null ) {
            Toast.makeText(this, "Card not found, register", Toast.LENGTH_SHORT).show();
            unregisteredCardUid.setText(cardUid);
        }
        fetchCards();
        startListener();

        // start bluetooth here, it should be started by broadcastreception
        // startService(new Intent(this, BluetoothConnectionService.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BluetoothConnectionService.class));
            startForegroundService(new Intent(this, DataHandlerService.class));
        } else {
            startService(new Intent(this, BluetoothConnectionService.class));
            startService(new Intent(this, DataHandlerService.class));
        }


        mBluetoothConnectionService = new BluetoothConnectionService();
        mServiceIntent = new Intent(this, mBluetoothConnectionService.getClass());
        if (!isMyServiceRunning(mBluetoothConnectionService.getClass())) {
            startService(mServiceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    public static ArrayList<CardDetails> getCards() {
        return cardDetailsList;
    }

    public void btnRegister(View view) {
        EditText unregisteredCardName = (EditText) findViewById(R.id.etNameOfCard);
        String cardName = unregisteredCardName.getText().toString();
        String cardUid = unregisteredCardUid.getText().toString();
        writeCardToDatabase(cardUid, cardName);
    }

    private void writeCardToDatabase(String cardUid, String cardname) {
        DataManager dm = new DataManager();
        CardDetails cd = new CardDetails();
        cd.setCardId(cardUid);
        cd.setName(cardname);
        ArrayList<CardDetails> al = new ArrayList<CardDetails>();
        al.add(cd);
        dm.writeCardDetailsListToFirebase(al, "carddetails");
    }

    private void startListener() {
        Log.d("MAIN", "Call USB listener");
        Intent intent = new Intent(MainActivity.this, ListenerService.class);
        startService(intent);
    }

    private void stopListener() {
        Log.d("MAIN", "Stop USB listener");
        stopService(new Intent(MainActivity.this, ListenerService.class));
    }


    public void btnOnClick(View view) {
        //  get the text
        Button btn = (Button) findViewById(view.getId());
        String cardUid = btn.getText().toString();
        //goHome();
        callAppstarter(cardUid);
    }


    public void goHome() {
        Log.d(LOG_TAG, "Go home ");

        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        4 * 1000, alarmIntent);
    }

    private void callAppstarter(String cardUid) {
        Log.d("MAINACTIVITY", "cardUid" + cardUid);
        Intent startAppIntent = new Intent(this, AppstarterService.class);
        startAppIntent.putExtra("CARDUID", cardUid);
        startService(startAppIntent);
    }

    private void fetchCards() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        databaseReference.child("carddetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // get all children at this level
                cardDetailsList.clear();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                //shake hands with all items
                String l = "prepare";
                for (DataSnapshot child : children) {
                    CardDetails cardDetails = child.getValue(CardDetails.class);
                    cardDetailsList.add(cardDetails);
                    l += "card id? " + cardDetails.getCardId();
                    l += "\ncard name? " + cardDetails.getName();
                    l += "\ncard uri? " + cardDetails.getUri();
                }
                Log.d("CENTRAL", "UPDATED CARDS " + l);
                cardsRecyclerViewAdapter.notifyDataSetChanged(); //update the screen
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

}