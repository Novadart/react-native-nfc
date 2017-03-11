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

    public static final String EVENT_NFC_DISCOVERED = "__NFC_DISCOVERED";

    // caches the last message received, to pass it to the listeners when it reconnects
    private WritableMap cachedNFCData;
    private boolean initialIntentProcessed = false;

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
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {

            switch (intent.getAction()){

                case NfcAdapter.ACTION_NDEF_DISCOVERED:
                    Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                    if (rawMessages != null) {
                        NdefMessage[] messages = new NdefMessage[rawMessages.length];
                        for (int i = 0; i < rawMessages.length; i++) {
                            messages[i] = (NdefMessage) rawMessages[i];
                        }
                        processNdefMessages(messages);
                    }
                    break;

                // ACTION_TAG_DISCOVERED is an unlikely case, according to https://developer.android.com/guide/topics/connectivity/nfc/nfc.html
                case NfcAdapter.ACTION_TAG_DISCOVERED:
                case NfcAdapter.ACTION_TECH_DISCOVERED:
                    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    processTag(tag);
                    break;

            }
        }
    }

    @ReactMethod
    public void getLatestNFCData(Callback callback){
        callback.invoke(DataUtils.cloneWritableMap(cachedNFCData));
    }


    private void sendEvent(@Nullable WritableMap payload) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NFC_DISCOVERED, payload); }


    private void processNdefMessages(NdefMessage[] messages){
        NdefProcessingTask task = new NdefProcessingTask();
        task.execute(messages);
    }

    private void processTag(Tag tag){
        TagProcessingTask task = new TagProcessingTask();
        task.execute(tag);
    }

    @Override
    public void onHostResume() {
        if(!initialIntentProcessed){
            if(getReactApplicationContext().getCurrentActivity() != null){ // it shouldn't be null but you never know
                // necessary because NFC might cause the activity to start and we need to catch that data too
                handleIntent(getReactApplicationContext().getCurrentActivity().getIntent());
            }
            initialIntentProcessed = true;
        }
    }

    @Override
    public void onHostPause() {}

    @Override
    public void onHostDestroy() {}


    private class NdefProcessingTask extends AsyncTask<NdefMessage[],Void,WritableMap> {

        @Override
        protected WritableMap doInBackground(NdefMessage[]... params) {
            NdefMessage[] messages = params[0];
            return NdefParser.parse(messages);
        }

        @Override
        protected void onPostExecute(WritableMap ndefData) {
            cachedNFCData = ndefData;
            sendEvent(ndefData);
        }
    }


    private class TagProcessingTask extends AsyncTask<Tag,Void,WritableMap> {

        @Override
        protected WritableMap doInBackground(Tag... params) {
            Tag tag = params[0];
            return TagParser.parse(tag);
        }

        @Override
        protected void onPostExecute(WritableMap tagData) {
            cachedNFCData = tagData;
            sendEvent(tagData);
        }
    }


}
