import 'package:flutter/material.dart';
import 'package:wowza_flutter_plugin/wowza_player.dart';


class Player extends StatefulWidget {
  @override
  _PlayerState createState() => _PlayerState();
}

class _PlayerState extends State<Player> {
  WowzaPlayerController wowzaController;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    WowzaPlayer player = new WowzaPlayer(
        onWowzaCreated: onWowzaCreated,
        sdkKey: "AAAA-AAAA-AAAA-AAAA-AAAA-AAAA",
        host: "AAAA.entrypoint.cloud.wowza.com",
        port: 1935,
        appName: "AAAA",
        streamName: "AAAA",
        userName: "AAAA",
        password: "AAAA");

    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Container(
          child: player,
          height: 300.0,
        ),
      ),
      floatingActionButton: FloatingActionButton(
        tooltip: 'play',
        child: Icon(Icons.play_circle_filled),
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
