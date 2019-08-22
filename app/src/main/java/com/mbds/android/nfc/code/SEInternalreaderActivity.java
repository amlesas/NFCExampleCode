package com.mbds.android.nfc.code;

import android.app.Activity;
import android.os.Bundle;
import android.se.omapi.Channel;
import android.se.omapi.Reader;
import android.se.omapi.SEService;
import android.se.omapi.Session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SEInternalreaderActivity extends Activity implements SEService.OnConnectedListener  {

    private SEService  seService;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requires parameters: Contex, Executor, Listener
        seService = new SEService(this, executor, this);
    }

    @Override
    public void onConnected() {
        try {	Reader[] readers = seService.getReaders();
            //is there a SE internal reader found?
            if (readers.length < 1){
                /*Throw an error reader not found*/
                return;
            }
            //check the names of the readers to find the reader you need HERE!
            //reader name: SIM1, SIM2, SD1, eSE1…
            Session session = readers[0].openSession();

            //opening the channel with the SE service AID
            byte[] aid = new byte[] {(byte) 0xF0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01}; //your AID here

            Channel channel = session.openBasicChannel(aid);

            //Build your APDU command HERE!
            byte[] commandAPDU = new byte[] {
                    (byte) 0x00, // CLA Class
                    (byte) 0xA4, // INS Instruction
                    (byte) 0x04, // P1  Parameter
                    (byte) 0x00, // P2  Parameter
                    (byte) 0x0A, // Lc Length of the data field (AID of the card service)
                    (byte) 0xF0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01, // AID
                    (byte) 0x00}; // Le Length of the expected response (no maximum size

            //transmit APDU command and receive APDU response
            byte[] responseAPDU = channel.transmit(commandAPDU);

            //check the status words and process...
            if (!(responseAPDU[responseAPDU.length-2] == (byte) 0x90 &&
                    responseAPDU[responseAPDU.length-1] == (byte) 0x00)) {
                // manage status error
            }

            //close channel when finished
            channel.close();

        } catch (Exception e) {
            /*manage thrown errors…*/
        }


    }
}
