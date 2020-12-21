package com.aspegrenide.klick;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {
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