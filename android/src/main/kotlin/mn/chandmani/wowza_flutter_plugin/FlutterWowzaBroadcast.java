package mn.chandmani.wowza_flutter_plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatus;
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatusCallback;

public class FlutterWowzaBroadcast implements PlatformView, MethodChannel.MethodCallHandler,WOWZBroadcastStatusCallback {

    Context context;
    BinaryMessenger messenger;
    MethodChannel channel;


    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    String host = "";
    int port = 1935;
    String appName = "";
    String streamName = "";
    String userName = "";
    String password = "";

    FlutterWowzaBroadcast(Context context, Object args, BinaryMessenger messenger, int id) {
        this.context = context;
        this.messenger = messenger;

        host = Utils.argument(args,"host");
        if(Utils.hasArgument(args,"port")) {
            port = Utils.argument(args,"port");
        }
        appName = Utils.argument(args,"appName");
        streamName = Utils.argument(args,"streamName");
        userName = Utils.argument(args,"userName");
        password = Utils.argument(args,"password");
        String sdkKey = Utils.argument(args,"sdkKey");


        channel = new MethodChannel(messenger, "wowza_flutter_plugin_" + id);

        channel.setMethodCallHandler(this);

        // Initialize the GoCoder SDK
        goCoder = WowzaGoCoder.init(context, sdkKey);

        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            if(goCoderInitError == null){
                Toast.makeText(context,
                        "GoCoder SDK error: Null" ,
                        Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(context,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WOWZAudioDevice();

        // Associate the WOWZCameraView defined in the U/I layout with the corresponding class member
        goCoderCameraView = getWowzaView(context);


    }

    @Override
    public View getView() {
        return goCoderCameraView;
    }

    @Override
    public void dispose() {

    }

    private WOWZCameraView getWowzaView(Context context) {
        WOWZCameraView webView = new WOWZCameraView(context);
        return webView;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "onResume":
                resume();
            case "toggle":

                if(goCoderBroadcaster == null) {
                    resume();
                }

                // Ensure the minimum set of configuration settings have been specified necessary to
                // initiate a broadcast streaming session
                WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

                if (configValidationError != null) {
                    Toast.makeText(context, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
                } else if (goCoderBroadcaster.getStatus().isBroadcasting()) {
                    // Stop the broadcast that is currently broadcasting
                    goCoderBroadcaster.endBroadcast();
                } else {
                    // Start streaming
                    goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
                }

                break;
            default:
                result.notImplemented();
        }

    }

    //
// The callback invoked upon changes to the state of the broadcast
//
    @Override
    public void onWZStatus(final WOWZBroadcastStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (goCoderStatus.getState()) {
            case READY:
                statusMessage.append("Ready to begin broadcasting");
                break;

            case BROADCASTING:
                statusMessage.append("Broadcast is active");
                break;

            case IDLE:
                statusMessage.append("The broadcast is stopped");
                break;

            default:
                return;
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    //
// The callback invoked when an error occurs during a broadcast
//
    @Override
    public void onWZError(final WOWZBroadcastStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void resume(){
        // Start the camera preview display
        if (goCoderCameraView != null) {
            if (goCoderCameraView.isPreviewPaused())
                goCoderCameraView.onResume();
            else
                goCoderCameraView.startPreview();
        }

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();

// Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1920x1080);

// Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
        goCoderBroadcastConfig.setHostAddress(host);
        goCoderBroadcastConfig.setPortNumber(port);
        goCoderBroadcastConfig.setApplicationName(appName);//app-f6da
        goCoderBroadcastConfig.setStreamName(streamName);
        goCoderBroadcastConfig.setUsername(userName);
        goCoderBroadcastConfig.setPassword(password);

// Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

// Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);
    }
}