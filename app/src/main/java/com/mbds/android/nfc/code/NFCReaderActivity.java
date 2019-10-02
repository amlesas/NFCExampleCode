package com.mbds.android.nfc.code;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;


import java.io.UnsupportedEncodingException;

public class NFCReaderActivity extends Activity {

    //Declare NfcAdapter and PendingIntent
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    public static String TAG = "TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…
            Toast.makeText(this, "NFC N'EST PAS ACTIVE", Toast.LENGTH_LONG).show();
            finish();

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
        } else {
            Toast.makeText(this, "NFC N'EST PAS ACTIVE", Toast.LENGTH_LONG).show();
            finish();
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

            String message = "AUCUNE INFORMATION TROUVEE SUR LE TAG !!!";

            // get the tag object from the received intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            //Get the Tag object information:
            //===============================
            // get the UTD from the tag
            byte[] uid = tag.getId();

            message = "Tag détecté UID : "+uid.toString();

            // get the technology list from the tag
            String[] technologies = tag.getTechList();
            // bit reserved to an optional file content descriptor
            int content = tag.describeContents();
            // get NDEF content
            Ndef ndef = Ndef.get(tag);
            // is the tag writable?
            boolean isWritable = ndef.isWritable();

            if (isWritable)
                message = message+" réinscriptible";
            else
                message = message+" non inscriptible";

            // can the tag be locked in writing?
            boolean canMakeReadOnly = ndef.canMakeReadOnly();

            if (canMakeReadOnly)
                message = message+", verrouillable en écriture";
            else
                message = message+", non verrouillable en écriture";

            //: get NDEF records:
            //===================
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            // check if the tag contains an NDEF message
            if (rawMsgs != null && rawMsgs.length != 0) {
                // instantiate a NDEF message array to get NDEF records
                NdefMessage[] ndefMessage = new NdefMessage[rawMsgs.length];
                // loop to get the NDEF records
                for (int i = 0; i < rawMsgs.length; i++) {
                    ndefMessage[i] = (NdefMessage) rawMsgs[i];
                    for (int j = 0; j < ndefMessage[i].getRecords().length; j++) {
                        NdefRecord ndefRecord = ndefMessage[i].getRecords()[j];

                        //parse NDEF record as String:
                        //============================
                        byte[] payload = ndefRecord.getPayload();
                        String encoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTf-8";
                        int languageSize = payload[0] & 0063;
                        try {
                            String recordTxt = new String(payload, languageSize + 1,
                                    payload.length - languageSize - 1, encoding);

                            message = message + ", NDEF MESSAGE : "+recordTxt;

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }



 /*                       //check NDEF record TNF:
                        //======================
                        switch(ndefRecord.getTnf()) {
                            case NdefRecord.TNF_ABSOLUTE_URI:
                                // manage NDEF record as an URI object
                                break;
                            case NdefRecord.TNF_EXTERNAL_TYPE:
                                // manage NDEF record as an URN (<domain_name>:<service_name>)
                                break;
                            case NdefRecord.TNF_MIME_MEDIA:
                                // manage NDEF record as the MIME type is:
                                // picture, video, sound, JSON, etc…
                                break;
                            case NdefRecord.TNF_WELL_KNOWN:
                                // manage NDEF record as the type is:
                                // contact (business card), phone number, email…
                                break;
                            default:
                                // manage NDEF record as text…
                        }
*/
                    }
                }
            }
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TAG, message);
            mainActivityIntent.putExtras(bundle);
            startActivity(mainActivityIntent);
            finish();
        }
    }
}
