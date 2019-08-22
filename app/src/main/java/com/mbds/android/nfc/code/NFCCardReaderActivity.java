package com.mbds.android.nfc.code;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.io.IOException;

public class NFCCardReaderActivity extends Activity {
    //Declare NfcAdapter and PendingIntent
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…

        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // single top flag avoids activity multiple instances launching
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Enable NFC foreground detection
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                // process error NFC not activated…
            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disable NFC foreground detection
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

    }

    public void OnNewIntent (Intent intent) {

        //Get the Tag object:
        //===================
        // retrieve the action from the received intent
        String action = intent.getAction();
        // check the event was triggered by the tag discovery
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            // get the tag object from the received intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // IsoDep implements connection to Tag type 4 (smard card type)
            IsoDep card = IsoDep.get(tag);

            //Connect to the card:
            //====================
            try {
                card.connect();
                byte[] selectAPDU = new byte[]{
                        (byte) 0x00, // CLA Class
                        (byte) 0xA4, // INS Instruction
                        (byte) 0x04, // P1  Parameter
                        (byte) 0x00, // P2  Parameter
                        (byte) 0x0A, // Lc Length of the data field (AID of the card service)
                        (byte) 0xF0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01, // AID
                        (byte) 0x00}; // Le Length of the expected response (no maximum size

                //Send the APDU command and get the APDU response:
                byte[] responseAPDU = card.transceive(selectAPDU);
                //Check the status word success:
                // Command success when SW1 = 0x90 and SW2 = 0x00 (2 last bytes)
                if (!(responseAPDU[responseAPDU.length-2] == (byte) 0x90 &&
                        responseAPDU[responseAPDU.length-1] == (byte) 0x00)) {
                    // manage status error
                }
                //And disconnect the card:
                card.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
