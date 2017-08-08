package com.novadart.reactnativenfc.parser;

import android.nfc.Tag;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.novadart.reactnativenfc.NfcDataType;

public class TagParser {

    public static WritableMap parse(String serialNumber, Tag tag){
        WritableMap result = new WritableNativeMap();

        result.putString("type", NfcDataType.TAG.name());
        result.putString("id", serialNumber);

        WritableMap data = new WritableNativeMap();

        WritableArray techList = new WritableNativeArray();
        String[] techListStr = tag.getTechList();
        if(techListStr != null){
            for (String s: techListStr) {
                techList.pushString(s);
            }
        }

        data.putArray("techList",techList);
        data.putString("description",tag.toString());
        data.putString("id", convertByteArrayToHexString(tag.getId()));
        result.putMap("data",data);
        return result;
    }

    private static String convertByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


}
