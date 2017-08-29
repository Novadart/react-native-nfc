package com.novadart.reactnativenfc.parser;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.util.Base64;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.novadart.reactnativenfc.NdefRecordType;
import com.novadart.reactnativenfc.NfcDataType;

import org.ndeftools.Message;
import org.ndeftools.MimeRecord;
import org.ndeftools.Record;
import org.ndeftools.wellknown.TextRecord;
import org.ndeftools.wellknown.UriRecord;

import java.util.Iterator;

public class NdefParser {

    public static WritableMap parse(String serialNumber, NdefMessage[] messages){
        WritableMap result = new WritableNativeMap();
        result.putString("type", NfcDataType.NDEF.name());
        result.putString("id", serialNumber);
        WritableArray data = new WritableNativeArray();
        if(messages != null) {
            for (NdefMessage m : messages) {
                try {
                    data.pushArray(parseMessage(m));
                } catch (FormatException | UnknownNdefRecordException ignored) {
                    // we skip it
                }
            }
        }
        result.putArray("data", data);
        return result;
    }


    /**
     *
     * @param message the NDEF message
     * @return array of converted records
     */
    private static WritableArray parseMessage(NdefMessage message) throws FormatException, UnknownNdefRecordException {
        WritableArray result = new WritableNativeArray();

        Message msg = new Message(message);

        Iterator<Record> iter = msg.iterator();
        while (iter.hasNext()){
            result.pushMap(parseRecord(iter.next()));
        }

        return result;
    }


    private static WritableMap parseRecord(Record record) throws UnknownNdefRecordException {
        if(record instanceof TextRecord){
            return parseTextRecord((TextRecord)record);
        } else if(record instanceof UriRecord){
            return parseUriRecord((UriRecord)record);
        } else if(record instanceof MimeRecord){
            return parseMimeRecord((MimeRecord)record);
        } else {
            throw new UnknownNdefRecordException();
        }
    }

    private static WritableMap parseTextRecord(TextRecord record){
        WritableMap result = new WritableNativeMap();
        result.putString("type", NdefRecordType.TEXT.name());
        result.putString("data",record.getText());
        result.putString("encoding",record.getEncoding() != null ? record.getEncoding().toString() : null);
        result.putString("locale",record.getLocale() != null ? record.getLocale().toString() : null);
        return result;
    }

    private static WritableMap parseUriRecord(UriRecord record){
        WritableMap result = new WritableNativeMap();
        result.putString("type", NdefRecordType.URI.name());
        result.putString("data", record.getUri() != null ? record.getUri().toString() : null);
        return result;
    }

    private static WritableMap parseMimeRecord(MimeRecord record){
        WritableMap result = new WritableNativeMap();
        result.putString("type", NdefRecordType.MIME.name());
        result.putString("data", record.getData() != null ? Base64.encodeToString(record.getData(), Base64.DEFAULT) : null);
        return result;
    }
}
