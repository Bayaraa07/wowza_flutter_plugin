import Flutter
import WebKit
import WowzaGoCoderSDK

public class FlutterWowzaBroadcast: NSObject, FlutterPlatformView, WOWZBroadcastStatusCallback {
    
    private weak var messenger: FlutterBinaryMessenger?
    var viewId: Int64 = 0
    var channel: FlutterMethodChannel?
    var goCoder:WowzaGoCoder?
    var myView: UIView?
    var observation: NSKeyValueObservation?
    
    init(messenger: FlutterBinaryMessenger?, withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: NSDictionary) {
        super.init()
        
        print("init FlutterWowzaBroadcast")
        
        self.messenger = messenger
        self.viewId = viewId
        myView = UIView(frame: frame)
        observation = myView!.layer.observe(\.bounds,options: [.old, .new]) { object, change in
            print("myDate changed from: \(change.oldValue!), updated to: \(change.newValue!)")
            self.goCoder?.cameraView = self.myView!
            self.goCoder!.cameraPreview?.start()
        }
        
        let sdkKey = args["sdkKey"] as! String
        if let goCoderLicensingError = WowzaGoCoder.registerLicenseKey(sdkKey) {
            self.showAlert("GoCoder SDK Licensing Error", error: goCoderLicensingError as NSError)
        }else{
            self.goCoder = WowzaGoCoder.sharedInstance()
        }
        if goCoder != nil{
            //Config
            let config = goCoder!.config
            config.load(WOWZFrameSizePreset.preset1280x720)
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
        self.channel?.setMethodCallHandler(handle)
        
        
    }
    
    deinit {
        print("FlutterWowzaBroadcast - dealloc")
        self.channel?.setMethodCallHandler(nil)
        observation?.invalidate()
        goCoder = nil
    }
    
    public func view() -> UIView {
        return myView!
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        let arguments  = call.arguments as? NSDictionary
        print("FlutterWowzaBroadcast setMethodCallHandler")
        
        switch call.method {
        case "toggle":
            toggle()
            break
        default:
            result(FlutterMethodNotImplemented)
            break
        }
    }
    

    
    func toggle(){
        
        print("FlutterWowzaBroadcast - toggle")
        if let configError = goCoder?.config.validateForBroadcast() {
            self.showAlert("Incomplete Streaming Settings", error: configError as NSError)
        } else if goCoder?.status.state != .broadcasting {
            goCoder?.startStreaming(self)
        } else {
            goCoder?.endStreaming(self)
        }
    }
    
    public func onWOWZStatus(_ status: WOWZBroadcastStatus!) {
        var message = ""
        switch (status.state) {
        case .idle:
            message = "The broadcast is idle"
        case .broadcasting:
            message = "Streaming is active"
        case .ready:
            message = "Broadcast components are ready"
        default: break
        }
        print(message)
    }
    
    public func onWOWZError(_ status: WOWZBroadcastStatus!) {
        // If an error is reported by the GoCoder SDK, display an alert dialog containing the error details
        DispatchQueue.main.async { () -> Void in
            self.showAlert("Live Streaming Error", status: status)
        }
    }
    
    func showAlert(_ title:String, status:WOWZBroadcastStatus) {
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
