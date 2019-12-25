# Flutter Wowza Plugin

Plugin that allows Flutter to communicate with a native Wowza SDK.

## Getting Started

For help getting started with Flutter, view our [online documentation](https://flutter.dev/docs)

#### Flutter
add in `pubspec.yaml`

```yaml
dependencies:
  
  # bottom of your dependencies
  wowza_flutter_plugin:
    git:
      url: git://github.com/Bayaraa07/wowza_flutter_plugin.git
```

#### iOS

In order for plugin to work correctly, you need to add new key to `ios/Runner/Info.plist`

```xml
<key>NSCameraUsageDescription</key>
<string>The camera will be used to capture video for live streaming.</string>
<key>NSMicrophoneUsageDescription</key>
<string>The microphone will be used to capture audio for live streaming.</string>
<key>io.flutter.embedded_views_preview</key>
<true/>
```

#### Android

In order for plugin to work correctly, you need to add permissions to `android/app/src/main/res/AndroidMenifest.xml`

```xml
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```
minimum SDK of 21


#### Example of Broadcast

```dart
import 'package:flutter/material.dart';
import 'package:wowza_flutter_plugin/wowza_broadcast.dart';


class Broadcast extends StatefulWidget {
  @override
  _BroadcastState createState() => _BroadcastState();
}

class _BroadcastState extends State<Broadcast> {
  WowzaBroadcastController wowzaController;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Container(
          child: WowzaBroadcast(
              onWowzaCreated: onWowzaCreated,
              sdkKey: "AAAA-AAAA-AAAA-AAAA-AAAA-AAAA",
              host: "AAAA.entrypoint.cloud.wowza.com",
              port: 1935,
              appName: "AAAA",
              streamName: "AAAA",
              userName: "AAAA",
              password: "AAAA"),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        tooltip: 'start broadcast',
        child: Icon(Icons.videocam),
        onPressed: () {
          this.wowzaController.toggle();
        },
      ),
    );
  }

  void onWowzaCreated(wowzaController) {
    this.wowzaController = wowzaController;
  }
}
```

sdkKey, host, apppName etc.
Replace your key
Please read more Wowza [documentation](https://www.wowza.com/docs/how-to-build-a-basic-app-with-gocoder-sdk-for-android)