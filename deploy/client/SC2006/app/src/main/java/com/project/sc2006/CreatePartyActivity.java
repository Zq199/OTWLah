/**
 * The CreateParty class provides functionality for generating a QR code
 * to represent a party session within the OTA Lah app. Party sessions are used
 * for group travel coordination and synchronization of arrivals at a shared destination.
 */
package com.project.sc2006;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.libraries.places.api.model.Place;
import com.project.sc2006.controllers.PartyModeController;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class CreatePartyActivity extends AppCompatActivity {
    private ImageView qrCodeIV;
    private ImageView createPartyBackBtn;
    private TextView partyIdDisplay;
    private Button shareQRBtn;
    private Place location;
    private String arrivalTime;
    private boolean isCancelled;
    private PartyModeController partyModeController;
    private String partyID;
    private String session;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);

        Intent i = getIntent();
        location = (Place)i.getExtras().get("location");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        arrivalTime = df.format(i.getExtras().get("time"));
        isCancelled = (boolean)i.getExtras().getBoolean("isCancelled");
        session = i.getExtras().getString("session");
        partyModeController = MainActivity.partyModeController;

        if (!isCancelled && savedInstanceState != null && savedInstanceState.containsKey("partyID")) partyID = savedInstanceState.getString("partyID");
        else {
            partyID = String.format("%04d", partyModeController.getPartyID());
            partyModeController.updatePartyCoordinates(session, CreatePartyActivity.this, location.getLatLng().latitude +","+location.getLatLng().longitude, arrivalTime);
        }

        createPartyBackBtn = findViewById(R.id.create_party_back);
        partyIdDisplay = findViewById(R.id.party_id);
        shareQRBtn = findViewById(R.id.qr_share_button);
        qrCodeIV = findViewById(R.id.idIVQrcode);

        partyIdDisplay.setText(partyID.charAt(0) + "  " + partyID.charAt(1) + "  " + partyID.charAt(2) + "  " + partyID.charAt(3));

        qrCodeIV.setImageBitmap(generateQR(partyID));

        createPartyBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatePartyActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(com.mancj.materialsearchbar.R.anim.fade_in_right, R.anim.fade_out_right);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (partyModeController != null) partyModeController.getPartyCoordinates(session, CreatePartyActivity.this);
            }
        }, 1000);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Intent i = getIntent();
        location = (Place)i.getExtras().get("location");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        arrivalTime = df.format(i.getExtras().get("time"));
        isCancelled = (boolean)i.getExtras().getBoolean("isCancelled");

        if (!isCancelled && savedInstanceState != null && savedInstanceState.containsKey("partyID")) partyID = savedInstanceState.getString("partyID");
        else {
            partyID = String.format("%04d", partyModeController.getPartyID());
            partyModeController.getPartyCoordinates(session, CreatePartyActivity.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent i = getIntent();
        isCancelled = i.getExtras().getBoolean("isCancelled");
        location = (Place)i.getExtras().get("location");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        arrivalTime = df.format(i.getExtras().get("time"));
        partyModeController.updatePartyCoordinates(session, CreatePartyActivity.this, location.getLatLng().latitude +","+location.getLatLng().longitude, arrivalTime);
        partyID = String.format("%04d", partyModeController.getPartyID());
        partyIdDisplay.setText(partyID.charAt(0) + "  " + partyID.charAt(1) + "  " + partyID.charAt(2) + "  " + partyID.charAt(3));

        qrCodeIV.setImageBitmap(generateQR(partyID));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (partyModeController != null) partyModeController.getPartyCoordinates(session, CreatePartyActivity.this);
            }
        }, 1000);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (partyID != null) {
            outState.putString("partyID", partyID);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Generates a QR code image based on the provided code.
     *
     * @param code The content or data that the QR code will represent.
     * @return A Bitmap image representing the QR code.
     */
    public Bitmap generateQR(String code){
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Display display = manager.getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        int width = point.x;
        int height = point.y;

        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        qrgEncoder = new QRGEncoder(partyID, null, QRGContents.Type.TEXT, dimen);

        bitmap = qrgEncoder.getBitmap(0);

        return bitmap;
    }
}