package com.aspegrenide.klick;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DataHandlerService extends Service {

    private static final String LOG_TAG = "KLICK DataHandlerServ";
    private static ArrayList<CardDetails> cardDetailsList = new ArrayList<CardDetails>();
    DatabaseReference mDatabase;


    public DataHandlerService() {
        fetchCards();
    }

    public ArrayList<CardDetails> getCardDetailsList() {
        return cardDetailsList;
    }

    public static ArrayList<CardDetails> getCards() {
        return cardDetailsList;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
                Log.d(LOG_TAG, "UPDATED CARDS " + l);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
