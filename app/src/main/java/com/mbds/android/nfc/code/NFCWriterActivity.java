package com.mbds.android.nfc.code;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class NFCWriterActivity extends Activity {

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


    @Override
    public void onNewIntent (Intent intent) {

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

            // create the NDEF mesage:
            //========================
            // dimension is the int number of entries of ndefRecords:
            int dimension = 1;
            NdefRecord[] ndefRecords = new NdefRecord[dimension];
            NdefMessage ndefMessage = new NdefMessage(ndefRecords);
            // Example with an URI NDEF record:
            String uriTxt = "http://www.mbds-fr.org"; // your URI in String format
            NdefRecord ndefRecord = NdefRecord.createUri(uriTxt);
            // Add the record to the NDEF message:
            ndefRecords[0] = ndefRecord;

            //Create NDEF message record type MIME:
            //=====================================
            String msgTxt = "Hello world!";
            String mimeType = "application/mbds.android.nfc"; // your MIME type
            ndefRecord = NdefRecord.createMime(mimeType,
                    msgTxt.getBytes(Charset.forName("US-ASCII")));
            //NDEF record URI type
             ndefRecord = NdefRecord.createUri(uriTxt);

            //NDEF record WELL KNOWN type (NFC Forum): TEXT
            //=============================================
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
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //check and write the tag received:
            //=================================
            // check the targeted tag the memory size and is the tag writable
            Ndef ndef = Ndef.get(tag);
            int size = ndefMessage.toByteArray().length;

            if (ndef!=null) {
                try {
                    ndef.connect();
                    if (!ndef.isWritable()) {
                        // tag is locked in writing!
                    }
                    if (ndef.getMaxSize() < size) {
                        // manage oversize!
                    }
                    // write the NDEF message on the tag
                    ndef.writeNdefMessage(ndefMessage);
                    ndef.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (FormatException e2) {
                    e2.printStackTrace();
                }
            }

            //check and write the tag received at activity:
            //=============================================
            // is the tag formatted?
             if (ndef == null) {
                 NdefFormatable format = NdefFormatable.get(tag);
                 if (format != null) {
                     // can you format the tag?
                     try {
                         format.connect();
                         //Format and write the NDEF message on the tag
                         format.format(ndefMessage);
                         //Example of tag locked in writing:
                         //formatable.formatReadOnly(message);
                         format.close();
                     } catch (IOException e1) {
                         e1.printStackTrace();
                     } catch (FormatException e2) {
                         e2.printStackTrace();
                     }
                 }
             }
        }
    }
}
