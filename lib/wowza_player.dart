import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef void WowzaCreatedCallback(WowzaPlayerController controller);

class WowzaPlayer extends StatefulWidget {

  final WowzaCreatedCallback onWowzaCreated;
  final host;
  final port;
  final appName;
  final streamName;
  final userName;
  final password;
  final sdkKey;

  WowzaPlayer({
    Key key,
    @required this.onWowzaCreated,
    @required this.host,
    @required this.port,
    @required this.appName,
    @required this.streamName,
    @required this.userName,
    @required this.password,
    @required this.sdkKey,
  });

  @override
  _WowzaState createState() => _WowzaState();
}

class _WowzaState extends State<WowzaPlayer> {
  @override
  Widget build(BuildContext context) {
    if(defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'wowza_flutter_plugin',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String,dynamic>{
          'broadcast': false,
          'host': widget.host,
          'port': widget.port,
          'appName': widget.appName,
          'streamName': widget.streamName,
          'userName': widget.userName,
          'password': widget.password,
          'sdkKey': widget.sdkKey,
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if(defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'wowza_flutter_plugin',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String,dynamic>{
          'broadcast': false,
          'host': widget.host,
          'port': widget.port,
          'appName': widget.appName,
          'streamName': widget.streamName,
          'userName': widget.userName,
          'password': widget.password,
          'sdkKey': widget.sdkKey,
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    }

    return new Text('$defaultTargetPlatform is not yet supported by this plugin');
  }
  Future<void> onPlatformViewCreated(id) async {
    if (widget.onWowzaCreated == null) {
      return;
    }
    widget.onWowzaCreated(new WowzaPlayerController.init(id));
  }
}

class WowzaPlayerController {

  MethodChannel _channel;

  WowzaPlayerController.init(int id) {
    _channel =  new MethodChannel('wowza_flutter_plugin_$id');
  }

  Future<void> toggle() async {
    return _channel.invokeMethod('toggle');
  }

  Future<void> resume() async {
    return _channel.invokeMethod('resume');
  }
}