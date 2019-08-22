package com.mbds.android.nfc.code;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class NDEFPushActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private NfcAdapter nfcAdapter;
    private NdefRecord ndefRecord;
    private NdefMessage ndefMessage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get default NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…

        }

        //Check NDEF push (Beam) is activated
        if (!nfcAdapter.isNdefPushEnabled()) {
            // ask user to activate Beam option before:
            // startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
            // finish the activity:
            // finish();
        }

        //NDEF record WELL KNOWN type (NFC Forum): TEXT
        //=============================================
        String msgTxt = "Hello world!";
        byte[] lang = new byte[0];
        byte[] data = new byte[0];
        int langeSize = 0;

        try {
            lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            langeSize = lang.length;
            data = new byte[0];
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            data = msgTxt.getBytes("UTF-8");
            int dataLength = data.length;
            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langeSize + dataLength);
            payload.write((byte) (langeSize & 0x1F));
            payload.write(lang, 0, langeSize);
            ndefRecord =  new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, new byte[0],
                    payload.toByteArray());
            ndefMessage = new NdefMessage(ndefRecord);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // subscribe to the callback for sending Beam message:
        nfcAdapter.setNdefPushMessageCallback(this, this);
        // then, you must implement NfcAdapter.createNdefMessageCallback…
        // instead of dynamic message, you can also set a static NDEF message:
        nfcAdapter.setNdefPushMessage(ndefMessage, this, this);
        // subscribe to the callback for the end of message receiving:
        nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        // then, you must implement NfcAdapter.onNdefPushComplete…

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Enable NFC foreground detection
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                // process error NFC not activated…
            }
            // subscribe to the callback for sending Beam message:
            nfcAdapter.setNdefPushMessageCallback(this, this);
            // then, you must implement NfcAdapter.createNdefMessageCallback…
            // instead of dynamic message, you can also set a static NDEF message:
            nfcAdapter.setNdefPushMessage(ndefMessage, this, this);
            // subscribe to the callback for the end of message receiving:
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
            // then, you must implement NfcAdapter.onNdefPushComplete…

        }
    }

    // implement the callback createNdefMessage
    public NdefMessage createNdefMessage(NfcEvent event) {
         return ndefMessage;
    }

    // implement the callback onNdefPushComplete
    public void onNdefPushComplete(NfcEvent arg0) {
        //Notify user the NDEF message was received
    }
}
