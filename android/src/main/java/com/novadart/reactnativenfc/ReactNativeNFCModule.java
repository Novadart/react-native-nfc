package com.novadart.reactnativenfc;


import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.novadart.reactnativenfc.parser.NdefParser;
import com.novadart.reactnativenfc.parser.TagParser;

public class ReactNativeNFCModule extends ReactContextBaseJavaModule implements ActivityEventListener,LifecycleEventListener {

    private static final String EVENT_NFC_DISCOVERED = "__NFC_DISCOVERED";

    // caches the last message received, to pass it to the listeners when it reconnects
    private WritableMap startupNfcData;
    private boolean startupNfcDataRetrieved = false;

    private boolean startupIntentProcessed = false;

    public ReactNativeNFCModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "ReactNativeNFC";
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {}

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent,false);
    }

    private void handleIntent(Intent intent, boolean startupIntent) {
        if (intent != null && intent.getAction() != null) {

            switch (intent.getAction()){

                case NfcAdapter.ACTION_NDEF_DISCOVERED:
                    Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                    if (rawMessages != null) {
                        NdefMessage[] messages = new NdefMessage[rawMessages.length];
                        for (int i = 0; i < rawMessages.length; i++) {
                            messages[i] = (NdefMessage) rawMessages[i];
                        }

                        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                        String serialNumber = getSerialNumber(tag);

                        processNdefMessages(serialNumber,messages,startupIntent);
                    }
                    break;

                // ACTION_TAG_DISCOVERED is an unlikely case, according to https://developer.android.com/guide/topics/connectivity/nfc/nfc.html
                case NfcAdapter.ACTION_TAG_DISCOVERED:
                case NfcAdapter.ACTION_TECH_DISCOVERED:
                    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String serialNumber = getSerialNumber(tag);

                    processTag(serialNumber,tag,startupIntent);
                    break;

            }
        }
    }

    /**
     * This method is used to retrieve the NFC data was acquired before the React Native App was loaded.
     * It should be called only once, when the first listener is attached.
     * Subsequent calls will return null;
     *
     * @param callback callback passed by javascript to retrieve the nfc data
     */
    @ReactMethod
    public void getStartUpNfcData(Callback callback){
        if(!startupNfcDataRetrieved){
            callback.invoke(DataUtils.cloneWritableMap(startupNfcData));
            startupNfcData = null;
            startupNfcDataRetrieved = true;
        } else {
            callback.invoke();
        }
    }


    private void sendEvent(@Nullable WritableMap payload) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NFC_DISCOVERED, payload);
    }

    private String getSerialNumber(Tag tag){
        byte[] id = tag.getId();
        String serialNumber = DataUtils.bytesToHex(id);

        return serialNumber;
    }

    private void processNdefMessages(String serialNumber, NdefMessage[] messages, boolean startupIntent){
        NdefProcessingTask task = new NdefProcessingTask(serialNumber, startupIntent);
        task.execute(messages);
    }

    private void processTag(String serialNumber, Tag tag, boolean startupIntent){
        TagProcessingTask task = new TagProcessingTask(serialNumber, startupIntent);
        task.execute(tag);
    }

    @Override
    public void onHostResume() {
        if(!startupIntentProcessed){
            if(getReactApplicationContext().getCurrentActivity() != null){ // it shouldn't be null but you never know
                // necessary because NFC might cause the activity to start and we need to catch that data too
                handleIntent(getReactApplicationContext().getCurrentActivity().getIntent(),true);
            }
            startupIntentProcessed = true;
        }
    }

    @Override
    public void onHostPause() {}

    @Override
    public void onHostDestroy() {}


    private class NdefProcessingTask extends AsyncTask<NdefMessage[],Void,WritableMap> {

        private final String serialNumber;
        private final boolean startupIntent;

        NdefProcessingTask(String serialNumber, boolean startupIntent) {
            this.serialNumber = serialNumber;
            this.startupIntent = startupIntent;
        }

        @Override
        protected WritableMap doInBackground(NdefMessage[]... params) {
            NdefMessage[] messages = params[0];
            return NdefParser.parse(serialNumber, messages);
        }

        @Override
        protected void onPostExecute(WritableMap ndefData) {
            if(startupIntent) {
                startupNfcData = ndefData;
            }
            sendEvent(ndefData);
        }
    }


    private class TagProcessingTask extends AsyncTask<Tag,Void,WritableMap> {

        private final String serialNumber;
        private final boolean startupIntent;

        TagProcessingTask(String serialNumber, boolean startupIntent) {
            this.serialNumber = serialNumber;
            this.startupIntent = startupIntent;
        }

        @Override
        protected WritableMap doInBackground(Tag... params) {
            Tag tag = params[0];
            return TagParser.parse(serialNumber, tag);
        }

        @Override
        protected void onPostExecute(WritableMap tagData) {
            if(startupIntent) {
                startupNfcData = tagData;
            }
            sendEvent(tagData);
        }
    }


}
