package com.mbds.android.nfc.code;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

public class MyHostAPDUService extends HostApduService {

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        //Process APDU command...
        //Return response and status words
        return new byte[0];
    }

    @Override
    public void onDeactivated(int i) {
        //initialize data and check everything is closed
    }
}
