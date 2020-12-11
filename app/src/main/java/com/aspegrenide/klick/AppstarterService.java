package com.aspegrenide.klick;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static androidx.core.view.KeyEventDispatcher.dispatchKeyEvent;


public class AppstarterService extends IntentService {
    public static final String CARDUID = "carduid";
    private static final String LOG_TAG = "KLICK AppstarterService";

    public AppstarterService() {
        super("AppstarterService");
        Log.d(LOG_TAG, "Init");
    }

    private CardDetails lookUpCardDetails(String cardUid) {
        ArrayList<CardDetails> cardDetailsList = DataHandlerService.getCards();
//        ArrayList<CardDetails> cardDetailsList = MainActivity.getCards();
        for (CardDetails cd: cardDetailsList) {
            if(cd.getCardId().equals(cardUid)) {
                Log.d(LOG_TAG, "Found match " + cd.getUri() + " id " + cd.getName());
                return (cd);
            }
        }
        // not recognized, call main activity and register the card
        Intent registerCard = new Intent(this, MainActivity.class);
        registerCard.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        registerCard.putExtra("REGISTER", cardUid);
        startActivity(registerCard);
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String cardUid = intent.getStringExtra("CARDUID");
        Log.d(LOG_TAG, "received carduid " + cardUid);
        //startBrowser(cardUid);
        CardDetails cd = lookUpCardDetails(cardUid);
        if (cd != null) {
            startApp(cd);
        }else {
            Log.d(LOG_TAG, "NO MATCH on cardUid: " + cardUid);
        }
    }

    // special case, go home to homescreen
    private void goHome() {
        Intent goHome= new Intent(Intent.ACTION_MAIN);
        goHome.addCategory(Intent.CATEGORY_HOME);
        goHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(goHome);
    }


    private void startApp(CardDetails cd) {
        String mUri = "";
        String mPkg = "";
        String mCls = "";
        String mAction = "";
        String mData = "";
        Log.d(LOG_TAG, "startApp: " + cd.getName());

        // if the card wants to go to homescreen
        if(cd.getAction() != null) {
            if (cd.getAction().equalsIgnoreCase("gohome")) {
                goHome();
                return;
            }
        }

        Intent myIntent;
        if(cd.getUri() != null) {
            myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cd.getUri()));
        } else {
            myIntent = new Intent(Intent.ACTION_VIEW);
        }
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (cd.getPkg() != null) {
            //special
            if((cd.getAction() == null) && (cd.getUri() == null)) {
                startNewActivity(getApplicationContext(), cd.getPkg());
                return;
            }
            myIntent.setPackage(cd.getPkg());
        }

        if ((cd.getCls() != null) && (cd.getPkg() != null)) {
            myIntent.setClassName(cd.getPkg(), cd.getCls());
        }

        if (cd.getAction() != null) {
            myIntent.setAction(cd.getAction());
        }

        if (cd.getData() != null) {
            myIntent.setData(Uri.parse(cd.getData()));
        }
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(LOG_TAG, "startApp: uri    " + cd.getUri());
        Log.d(LOG_TAG, "startApp: action " + cd.getAction());
        Log.d(LOG_TAG, "startApp: data   " + cd.getData());
        Log.d(LOG_TAG, "startApp: cls    " + cd.getCls());
        Log.d(LOG_TAG, "startApp: pkg    " + cd.getPkg());

        startActivity(myIntent);
    }


    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
