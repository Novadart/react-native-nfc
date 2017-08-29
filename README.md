# Reading NFC tags for React Native (Android only)

This project has the goal of making it easy (or easier) to scan NFC tags and read the NDEF records they contain.

To read the NDEF data it makes use of the library **[ndef-tools-for-android](https://github.com/skjolber/ndef-tools-for-android)**.


## Requirements
This library is compatible and was tested with React Native projects with version >= 0.40.0


## Installation

Install the plugin via NPM:
```
$ npm install react-native-nfc --save
    
```

and then link it:

```
$ react-native link react-native-nfc
```

## Configuration

Take a moment to read [this Android documentation](https://developer.android.com/guide/topics/connectivity/nfc/nfc.html) about NFC Basics, especially
the *[How NFC Tags are Dispatched to Applications](https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#dispatching)* section.

### Edit the file AndroidManifest.xml

Add the permission to read NFC data:

```xml
<uses-permission android:name="android.permission.NFC" />
```

Add the following attribute to your `<activity>` section to ensure that all NFC intents are delivered to the same activity.

```
android:launchMode="singleTask"
```

Add the following intent filters and metadata tag to instruct Android that you want to catch NFC intents that contain NDEF 
records and generic payloads about NFC tech, as a fallback in case NDEF messages could not be parsed (see [here](https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#dispatching) 
for more info about this).

```xml
<intent-filter>
    <action android:name="android.nfc.action.NDEF_DISCOVERED"/>
    <category android:name="android.intent.category.DEFAULT"/>
</intent-filter>

<intent-filter>
    <action android:name="android.nfc.action.TECH_DISCOVERED"/>
</intent-filter>

<meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/nfc_tech_filter" />
```

Create the file `android/src/main/res/xml/nfc_tech_filter.xml` and add the following content:

```xml
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <tech-list>
        <tech>android.nfc.tech.IsoDep</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcA</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcB</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcF</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcV</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.Ndef</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NdefFormatable</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.MifareClassic</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.MifareUltralight</tech>
    </tech-list>
</resources>
```

### Example AndroidManifest.xml
 ```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.reactnativenfcdemo"
          android:versionCode="1"
          android:versionName="1.0">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.NFC" />

    <uses-sdk
            android:minSdkVersion="16"
            android:targetSdkVersion="22" />

    <application
            android:name=".MainApplication"
            android:allowBackup="true"
            android:label="@string/app_name"
            android:icon="@mipmap/ic_launcher"
            android:theme="@style/AppTheme">
        <activity
                android:name=".MainActivity"
                android:screenOrientation="portrait"
                android:label="@string/app_name"
                android:launchMode="singleTask"
                android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                android:windowSoftInputMode="adjustResize">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.nfc.action.NDEF_DISCOVERED"/>
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>

            <intent-filter>
                <action android:name="android.nfc.action.TECH_DISCOVERED"/>
            </intent-filter>

            <meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/nfc_tech_filter" />

        </activity>
        <activity android:name="com.facebook.react.devsupport.DevSettingsActivity" />
    </application>

</manifest>

```


## Usage

What you need to do is to register a listener on the NFC module, like this:

```
function listener(payload){
    // TODO
}

NFC.addListener(listener);
```


This is a more complex example:

```javascript
import NFC, {NfcDataType, NdefRecordType} from "react-native-nfc";
import React, {Component} from "react";
import {ToastAndroid} from "react-native";

NFC.addListener((payload) => {

    switch (payload.type) {
        
        case NfcDataType.NDEF:
            let messages = payload.data;
            for (let i in messages) {
                let records = messages[i];
                for (let j in records) {
                    let r = records[j];
                    if (r.type === NdefRecordType.TEXT) {
                        // do something with the text data
                    } else {
                        ToastAndroid.show(
                            `Non-TEXT tag of type ${r.type} with data ${r.data}`,
                            ToastAndroid.SHORT
                        );
                    }
                }
            }
            break;
            
        case NfcDataType.TAG:
            ToastAndroid.show(
                `The TAG is non-NDEF:\n\n${payload.data.description}`,
                ToastAndroid.SHORT
            );
            break;
    }

});


// ... the rest of the app code

```

Notice:
Once you've integrated the plugin in this way you'll be able to receive the data read via NFC by your Android device. 
You will receive the data *even if your app is closed (or killed)* and is started as a consequence of a NFC event.
If you want to receive the data in a given time,just change the position where you addListener to NFC,such as doing it in the componentDidMount in a page of your program.

```javascript
import NFC, {NfcDataType, NdefRecordType} from "react-native-nfc";

export default class NfcScanPage extends Component {

  constructor(props){
    super(props);
  }

    render() {
        return (
              ....
        );
    }

    componentDidMount(){
      this.bindNfcListener();
    }

    bindNfcListener(){
      NFC.addListener((payload)=>{
        alert(payload.data.id);
      })
    }


}


```

The listener receives a JSON object that has a **type** property with possible values:

* **NfcDataType.NDEF** - if the NFC tag contains NDEF data
* **NfcDataType.TAG** - if the NFC tag did not contain NDEF data (or could not be parsed) hence we get just info about the TAG



### NDEF Payload format

Property | Values
--- | --- 
type | Always **NfcDataType.NDEF**
id   | The id of the tag in hex format.
data | Contains an array of messages. Each message is an array of records.
 
 

#### NDEF Records format
Each record object contains always the properties *type* and *data*.

Here is the list of currently supported records:

Type | Data | Other properties
--- | --- | ---
NdefRecordType.TEXT | The text string | *encoding* and *locale*
NdefRecordType.URI | The URI string | -
NdefRecordType.MIME | Base64 data of the mime data | -



### TAG Payload format

Property | Values
--- | --- 
type | Always **NfcDataType.TAG**
techList | List of strings about the discoverred tech
description | string description of the tag useful for debug


## TODO

* Support more record types
* Support writing tags
* Advanced NFC operations
