package com.novadart.reactnativenfc.parser;

import android.nfc.Tag;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

public class TagParser {

    public static WritableMap parse(Tag tag){
        WritableMap result = new WritableNativeMap();

        result.putString("type","TAG");

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

        result.putMap("data",data);
        return result;
    }

}
