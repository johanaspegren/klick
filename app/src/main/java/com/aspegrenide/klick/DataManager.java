package com.aspegrenide.klick;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class DataManager {

    DatabaseReference mDatabase;

    public void writeCardDetailsToFirebase(CardDetails cardDetails, String dataBaseSet) {
        Log.d("DM", "write to database " + cardDetails.toString());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(dataBaseSet).child(cardDetails.getCardId()).setValue(cardDetails);
    }
}
