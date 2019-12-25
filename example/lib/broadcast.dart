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
              sdkKey: "GOSK-4847-010C-DE12-C892-2525",
              host: "cb8a37.entrypoint.cloud.wowza.com",
              port: 1935,
              appName: "app-f6da",
              streamName: "26f8f354",
              userName: "client47754",
              password: "60119cab")
          ,
          height: 300.0,
          width: 200.0,
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
    //this.wowzaController.toggle();
  }
}
