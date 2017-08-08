package com.novadart.reactnativenfc;


import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

public class DataUtils {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];

      for ( int j = 0; j < bytes.length; j++ ) {
          int v = bytes[j] & 0xFF;

          hexChars[j * 2] = hexArray[v >>> 4];
          hexChars[j * 2 + 1] = hexArray[v & 0x0F];
      }

      return new String(hexChars);
    }

    public static WritableMap cloneWritableMap(WritableMap map){
        if(map == null){
            return null;
        }
        WritableMap r = new WritableNativeMap();

        ReadableMapKeySetIterator iter = map.keySetIterator();
        while (iter.hasNextKey()){
            String key = iter.nextKey();
            ReadableType type = map.getType(key);
            switch (type){
                case Array: r.putArray(key, cloneReadableArray(map.getArray(key))); break;
                case Boolean: r.putBoolean(key, map.getBoolean(key)); break;
                case Map: r.putMap(key, cloneReadableMap(map.getMap(key))); break;
                case Null: r.putNull(key); break;
                case Number: r.putDouble(key, map.getDouble(key)); break;
                case String: r.putString(key, map.getString(key)); break;
            }
        }
        return r;
    }


    public static WritableMap cloneReadableMap(ReadableMap map){
        if(map == null){
            return null;
        }
        WritableMap r = new WritableNativeMap();

        ReadableMapKeySetIterator iter = map.keySetIterator();
        while (iter.hasNextKey()){
            String key = iter.nextKey();
            ReadableType type = map.getType(key);
            switch (type){
                case Array: r.putArray(key, cloneReadableArray(map.getArray(key))); break;
                case Boolean: r.putBoolean(key, map.getBoolean(key)); break;
                case Map: r.putMap(key, cloneReadableMap(map.getMap(key))); break;
                case Null: r.putNull(key); break;
                case Number: r.putDouble(key, map.getDouble(key)); break;
                case String: r.putString(key, map.getString(key)); break;
            }
        }
        return r;
    }


    public static WritableArray cloneWritableArray(WritableArray arr){
        if(arr == null){
            return null;
        }
        WritableArray r = new WritableNativeArray();

        for (int i=0; i<arr.size(); i++){
            ReadableType type = arr.getType(i);
            switch (type){
                case Array: r.pushArray(cloneReadableArray(arr.getArray(i))); break;
                case Boolean: r.pushBoolean(arr.getBoolean(i)); break;
                case Map: r.pushMap(cloneReadableMap(arr.getMap(i))); break;
                case Null: r.pushNull(); break;
                case Number: r.pushDouble(arr.getDouble(i)); break;
                case String: r.pushString(arr.getString(i)); break;
            }
        }

        return r;
    }


    public static WritableArray cloneReadableArray(ReadableArray arr){
        if(arr == null){
            return null;
        }
        WritableArray r = new WritableNativeArray();

        for (int i=0; i<arr.size(); i++){
            ReadableType type = arr.getType(i);
            switch (type){
                case Array: r.pushArray(cloneReadableArray(arr.getArray(i))); break;
                case Boolean: r.pushBoolean(arr.getBoolean(i)); break;
                case Map: r.pushMap(cloneReadableMap(arr.getMap(i))); break;
                case Null: r.pushNull(); break;
                case Number: r.pushDouble(arr.getDouble(i)); break;
                case String: r.pushString(arr.getString(i)); break;
            }
        }

        return r;
    }

}
