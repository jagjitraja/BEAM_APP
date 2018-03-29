package com.example.t00533766.beam;

import android.os.Bundle;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;


public class MainActivity extends Activity implements CreateNdefMessageCallback,
        OnNdefPushCompleteCallback {
    NfcAdapter mNfcAdapter;
    TextView mInfoText;

    private String TAG = "*********************************************";
    
    private static final int MESSAGE_SENT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoText = (TextView) findViewById(R.id.text_view);
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            mInfoText = (TextView) findViewById(R.id.text_view);
            mInfoText.setText("NFC is not available on this device.");
            return;
        }
        // Register callback to set NDEF message
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        
        if (mNfcAdapter.isNdefPushEnabled()){
            Log.d(TAG, "onCreate: PUSH ENABLED");
        }




        Bundle bundle = getIntent().getExtras();
        if (bundle!=null)
        Log.d(TAG, "onNewIntent: =========="+bundle.size());
    }


    /**
     * Implementation for the CreateNdefMessageCallback interface
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Log.d(TAG, "createNdefMessage: ");
        Time time = new Time();
        time.setToNow();
        String text = ("Beam me up!\n\n" +
                "Beam Time: " + time.format("%H:%M:%S"));

        return new NdefMessage(
                new NdefRecord[] {
                        createMimeRecord(text.getBytes())
                        //,NdefRecord.createApplicationRecord("com.example.t00533766.beam"),
                });
    }

    /**
     * Implementation for the OnNdefPushCompleteCallback interface
     */
    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
        Log.d(TAG, "onNdefPushComplete: ");
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: ");
            switch (msg.what) {
                case MESSAGE_SENT:
                    Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " +getIntent().getAction());
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
            Log.d(TAG, "onResume: =----------=-=-=-=-=-");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");

        Bundle bundle = intent.getExtras();

        Log.d(TAG, "onNewIntent: =========="+bundle.size());

        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Log.d(TAG, "processIntent: ");
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        // only one message sent during the beam
        if (rawMsgs!=null) {
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            Log.d(TAG, "processIntent: ");
            mInfoText.setText(new String(msg.getRecords()[0].getPayload()));
        }
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     */
    public NdefRecord createMimeRecord(byte[] payload) {
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, payload, new byte[0], payload);
        Log.d(TAG, "createMimeRecord: ");

        Log.d(TAG, "createMimeRecord: "+mimeRecord.toMimeType()+"           "+ Arrays.toString(mimeRecord.getPayload()));
        return mimeRecord;
    }

}

