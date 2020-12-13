package com.aspegrenide.klick;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterCardActivity extends AppCompatActivity {
    public static final String CARDNOTFOUND = "CARDNOTFOUND";
    private static final String TAG = "REGISTER";
    // Bluetooth's variables

    TextView tvCardUid;
    TextView tvCarduName;
    TextView tvCarduUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registercard);
        Log.d(TAG, "init register");

        tvCardUid = (TextView) findViewById(R.id.tvCardUid);
        tvCarduName = (TextView) findViewById(R.id.tvCardName);
        tvCarduUri = (TextView) findViewById(R.id.tvCardUri);
        String failedCardId;

        Bundle extras = getIntent().getExtras();
        String cardUid = extras.getString(MainActivity.CARDNOTFOUND);
        tvCardUid.setText(cardUid);
        Log.d(TAG, "carduid of missing vard is = " + tvCardUid);
    }

    public void registerCard(View view) {
        String cardUid = tvCardUid.getText().toString();
        String cardName = tvCarduName.getText().toString();
        String cardUri = tvCarduUri.getText().toString();

        writeCardToDatabase(cardUid, cardName, cardUri);

    }

    private void writeCardToDatabase(String cardUid, String cardName, String cardUri) {
        DataManager dm = new DataManager();
        CardDetails cd = new CardDetails();
        cd.setCardId(cardUid);
        cd.setName(cardName);
        cd.setUri(cardUri);
        dm.writeCardDetailsToFirebase(cd, MainActivity.CARD_DETAILS);
    }

}