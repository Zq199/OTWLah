/**
 * The JoinParty class provides functionality for users to join an existing party session
 * within the OTA Lah app. Users can join a party by entering a PIN number or scanning a
 * QR code associated with the party session. This class facilitates the process of party
 * participation and synchronization of travel plans with others.
 */
package com.project.sc2006;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboard;
import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.project.sc2006.controllers.PartyModeController;

import io.github.g00fy2.quickie.QRResult;
import io.github.g00fy2.quickie.QRResult.QRError;
import io.github.g00fy2.quickie.QRResult.QRMissingPermission;
import io.github.g00fy2.quickie.QRResult.QRSuccess;
import io.github.g00fy2.quickie.QRResult.QRUserCanceled;
import io.github.g00fy2.quickie.ScanCustomCode;
import io.github.g00fy2.quickie.ScanQRCode;
import io.github.g00fy2.quickie.config.BarcodeFormat;
import io.github.g00fy2.quickie.config.ScannerConfig;
import io.github.g00fy2.quickie.content.QRContent;


public class JoinPartyActivity extends AppCompatActivity implements NumberKeyboardListener {
    private String code = new String();
    TextView partyCode1;
    TextView partyCode2;
    TextView partyCode3;
    TextView partyCode4;

    String session;

    private ActivityResultLauncher<Void> scanQrCode = registerForActivityResult(new ScanQRCode(), result -> {
        handleScanResult((QRResult) result);
    });

    private void scanQR() {
        scanQrCode.launch(null);
    }

    // Method to handle QR code scan results
    private void handleScanResult(QRResult result) {
        if (result instanceof QRResult.QRSuccess) {
            QRContent qrContent = ((QRResult.QRSuccess) result).getContent();
            String rawValue = qrContent.getRawValue(); // Use the raw value or handle other types of content
            try {
                int partyID = Integer.parseInt(rawValue);
                Toast.makeText(this, "Joining Party: " + partyID, Toast.LENGTH_SHORT).show();
                MainActivity.partyModeController.joinParty(session, rawValue, JoinPartyActivity.this);
                Intent data = new Intent();
                String text = "123";
                data.setData(Uri.parse(text));
                setResult(RESULT_OK, data);
                finish();
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Format Error: " + rawValue, Toast.LENGTH_SHORT).show();
            }
        } else if (result instanceof QRResult.QRUserCanceled) {
            Toast.makeText(this, "Scanned canceled", Toast.LENGTH_SHORT).show();
        } else if (result instanceof QRResult.QRMissingPermission) {
            Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show();
        } else if (result instanceof QRResult.QRError) {
            Exception error = ((QRResult.QRError) result).getException();
            Toast.makeText(this, "Error " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_party);

        Bundle extras = getIntent().getExtras();

        session = extras.getString("session");

        partyCode1 = findViewById(R.id.party_code_1_txt);
        partyCode2 = findViewById(R.id.party_code_2_txt);
        partyCode3 = findViewById(R.id.party_code_3_txt);
        partyCode4 = findViewById(R.id.party_code_4_txt);
        NumberKeyboard keyboard = findViewById(R.id.numberKeyboard);
        ImageView backBtn = findViewById(R.id.join_party_back);

        keyboard.setListener(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button scanQRBtn = findViewById(R.id.scanQRButton);

        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR();
            }
        });


    }

    @Override
    public void onLeftAuxButtonClicked() {
        //No such button
    }

    @Override
    public void onNumberClicked(int number) {
        if (code.length() < 4) {
            code += Integer.toString(number);

            switch (code.length()) {
                case 1:
                    partyCode1.setText(Integer.toString(number));
                    break;
                case 2:
                    partyCode2.setText(Integer.toString(number));
                    break;
                case 3:
                    partyCode3.setText(Integer.toString(number));
                    break;
                case 4:
                    partyCode4.setText(Integer.toString(number));
                    break;
            }
            if(code.length() == 4){
                String code = partyCode1.getText().toString() + partyCode2.getText().toString() + partyCode3.getText().toString() + partyCode4.getText().toString();
                MainActivity.partyModeController.joinParty(session, code, JoinPartyActivity.this);
                Intent data = new Intent();
                String text = "123";
                data.setData(Uri.parse(text));
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    @Override
    public void onRightAuxButtonClicked() {
        if (code.length() > 0) {
            switch (code.length()) {
                case 1:
                    partyCode1.setText("");
                    break;
                case 2:
                    partyCode2.setText("");
                    break;
                case 3:
                    partyCode3.setText("");
                    break;
                case 4:
                    partyCode4.setText("");
                    break;
            }
            code = code.substring(0, code.length() - 1);
        }
    }
}