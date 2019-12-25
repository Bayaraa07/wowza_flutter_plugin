import Flutter
import UIKit

public class SwiftWowzaFlutterPlugin: NSObject, FlutterPlugin {
  
    public static func register(with registrar: FlutterPluginRegistrar) {
        print("register plugin")
        registrar.register(WowzaFactory(messenger: registrar.messenger()) as FlutterPlatformViewFactory, withId: "wowza_flutter_plugin")
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
