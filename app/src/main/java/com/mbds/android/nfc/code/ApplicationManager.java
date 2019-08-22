package com.mbds.android.nfc.code;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

public class ApplicationManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Check device is NFC feature enable
        PackageManager pkManager = getPackageManager();
        if (!pkManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // manage NFC service unavailability
        }

        // Check NFC feature is activated

        NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = nfcManager.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            // ask the user to turn on NFC}


        }
    }
}
