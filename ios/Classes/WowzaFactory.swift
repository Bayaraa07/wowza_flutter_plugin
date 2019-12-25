import Flutter

public class WowzaFactory: NSObject, FlutterPlatformViewFactory{
    private var messenger: FlutterBinaryMessenger?
    
    init(messenger: FlutterBinaryMessenger?){
        super.init()
        self.messenger = messenger
    }
    
    public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
    
    public func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
        let arguments = args as! NSDictionary
        
        let broadcast = arguments["broadcast"] as! Bool
        if broadcast {
            
            return FlutterWowzaBroadcast(messenger: messenger,
                                         withFrame: frame,
                                         viewIdentifier: viewId,
                                         arguments: arguments)
        }else {
            return FlutterWowzaPlayer(messenger: messenger,
                                      withFrame: frame,
                                      viewIdentifier: viewId,
                                      arguments: arguments)
        }
        
    }
    
}
