import Flutter
import WebKit
import WowzaGoCoderSDK

public class FlutterWowzaPlayer: NSObject, FlutterPlatformView, WOWZPlayerStatusCallback {
    
    private weak var messenger: FlutterBinaryMessenger?
    var viewId: Int64 = 0
    var channel: FlutterMethodChannel?
    var goCoder:WowzaGoCoder?
    var myView: UIView?
    lazy var player = WOWZPlayer()
    
    init(messenger: FlutterBinaryMessenger?, withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: NSDictionary) {
        super.init()
        
        print("init FlutterWowzaPlayer")
        
        self.messenger = messenger
        self.viewId = viewId
        myView = UIView(frame: frame)
        
        let sdkKey = args["sdkKey"] as! String
        
        if let goCoderLicensingError = WowzaGoCoder.registerLicenseKey(sdkKey) {
            self.showAlert("GoCoder SDK Licensing Error", error: goCoderLicensingError as NSError)
        }else{
            self.goCoder = WowzaGoCoder.sharedInstance()
        }
        if goCoder != nil{
            //Config
            let config = goCoder!.config
            config.load(WOWZFrameSizePreset.preset352x288)
            // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
            config.hostAddress = args["host"] as? String
            config.portNumber = args["port"] as! UInt
            config.applicationName = args["appName"] as? String
            config.streamName = args["streamName"] as? String
            config.username = args["userName"] as? String
            config.password = args["password"] as? String
            goCoder?.config = config
        }
        
        
        let channelName = "wowza_flutter_plugin_" + String(viewId)
        self.channel = FlutterMethodChannel(name: channelName, binaryMessenger: messenger!)
        self.channel?.setMethodCallHandler({
            [weak self] (call: FlutterMethodCall, result: FlutterResult) -> Void in
            
            let arguments  = call.arguments as? NSDictionary
            print("called setMethodCallHandler")
            
            switch call.method {
            case "toggle":
                self?.toggle()
            default:
                result(FlutterMethodNotImplemented)
                break
            }
        })
        
        
    }
    
    deinit {
        print("FlutterWebViewController - dealloc")
        self.channel?.setMethodCallHandler(nil)
        goCoder = nil
    }
    
    public func view() -> UIView {
        return myView!
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        result("iOS " + UIDevice.current.systemVersion)
    }
    
    func toggle(){
        if(self.player.currentPlayState() == WOWZPlayerState.idle){
            print("Connecting...")
            self.player.play(goCoder!.config, callback: self)
        }else{
            self.player.resetPlaybackErrorCount()
            self.player.stop()
        }
        
    }
    public func onWOWZStatus(_ status: WOWZPlayerStatus!) {
        var message = ""
        switch (status.state) {
        case .idle:
            message = "The broadcast is idle"
            break
            
        case .connecting:
            message = "The broadcast is connecting"
            self.player.playerView = self.myView;
            break;
        case .playing:
            message = "The broadcast is playing"
            break;
        case .stopping:
            message = "The broadcast is stopping"
            break;
            
        case .buffering:
            message = "The broadcast is buffering"
            break;
            
        default: break
        }
        print(message)
    }
    public func onWOWZError(_ status: WOWZPlayerStatus!) {
        // If an error is reported by the GoCoder SDK, display an alert dialog containing the error details
        DispatchQueue.main.async { () -> Void in
            self.showAlert("Live Streaming Error", status: status)
        }
    }
    
    func showAlert(_ title:String, status:WOWZPlayerStatus) {
        let alertController = UIAlertController(title: title, message: status.description, preferredStyle: .alert)
        
        let action = UIAlertAction(title: "OK", style: .default, handler: nil)
        alertController.addAction(action)
        
        myView?.window?.rootViewController?.present(alertController, animated: true, completion: nil)
    }
    
    func showAlert(_ title:String, error:NSError) {
        let alertController = UIAlertController(title: title, message: error.localizedDescription, preferredStyle: .alert)
        
        let action = UIAlertAction(title: "OK", style: .default, handler: nil)
        alertController.addAction(action)
        
        myView?.window?.rootViewController?.present(alertController, animated: true, completion: nil)
    }
}
