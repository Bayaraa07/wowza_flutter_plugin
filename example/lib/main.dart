import 'package:flutter/material.dart';
import 'package:wowza_flutter_plugin_example/broadcast.dart';
import 'package:wowza_flutter_plugin_example/player.dart';

void main() {
  runApp(MaterialApp(
    title: 'Navigation Basics',
    home: MyApp(),
  ));
}


class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Welcome to Flutter'),
      ),
      body: Center(
          child: Column(
            children: <Widget>[
              RaisedButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => Broadcast()),
                  );
                },
                child: Text('Broadcast Button', style: TextStyle(fontSize: 20)),
              ),
              RaisedButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => Player()),
                  );
                },
                child: Text('Play Button', style: TextStyle(fontSize: 20)),
              ),
            ],
          )),
    );
  }
}
