'use strict';

import { NativeModules, DeviceEventEmitter } from 'react-native';

export const NfcDataType = {
    NDEF : "NDEF",
    TAG : "TAG"
};

export const NdefRecordType = {
    TEXT : "TEXT",
    URI : "URI",
    MIME : "MIME"
};

const NFC_DISCOVERED = '__NFC_DISCOVERED';
let _registeredToEvents = false;
let _listeners = {};

let _registerToEvents = () => {
    if(!_registeredToEvents){
        NativeModules.ReactNativeNFC.getStartUpNfcData(_notifyListeners);
        DeviceEventEmitter.addListener(NFC_DISCOVERED, _notifyListeners);
        _registeredToEvents = true;
    }
};

let _notifyListeners = (data) => {
    if(data){
        for(let _listener in _listeners){
            _listeners[_listener](data);
        }
    }
};

const NFC = {};

NFC.addListener = (name, callback) => {
    _listeners[name] = callback;
    _registerToEvents();
};

NFC.removeListener = (name) => {
    delete _listeners[name];
};

NFC.removeAllListeners = () => {
    DeviceEventEmitter.removeAllListeners(NFC_DISCOVERED);
    _listeners = {};
    _registeredToEvents = false;
};

export default NFC;