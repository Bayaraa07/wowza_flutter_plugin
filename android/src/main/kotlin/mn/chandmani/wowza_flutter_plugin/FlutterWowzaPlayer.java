package mn.chandmani.wowza_flutter_plugin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.configuration.WowzaConfig;
import com.wowza.gocoder.sdk.api.data.WOWZDataEvent;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatus;
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatusCallback;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterWowzaPlayer implements PlatformView, MethodChannel.MethodCallHandler, WOWZPlayerStatusCallback {
    final private static String TAG = "WowzaPlayer";

    Context context;
    BinaryMessenger messenger;
    MethodChannel channel;

    String host = "";
    int port = 1935;
    String appName = "";
    String streamName = "";
    String userName = "";
    String password = "";


    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;
    // Stream player view
    private WOWZPlayerView mStreamPlayerView = null;
    private WOWZPlayerConfig mStreamPlayerConfig = null;

    private ProgressDialog mBufferingDialog = null;
    private ProgressDialog mGoingDownDialog =null;

    private VolumeChangeObserver mVolumeSettingChangeObserver = null;


    FlutterWowzaPlayer(Context context, Object args, BinaryMessenger messenger, int id) {
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

        // Associate the WOWZCameraView defined in the U/I layout with the corresponding class member
        mStreamPlayerView = getWowzaView(context);


        /*
            Packet change listener setup
             */
        WOWZPlayerView.PacketThresholdChangeListener packetChangeListener = new WOWZPlayerView.PacketThresholdChangeListener() {
            @Override
            public void packetsBelowMinimumThreshold(int packetCount) {
                WOWZLog.debug("Packets have fallen below threshold "+packetCount+"... ");

//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(activity, "Packets have fallen below threshold ... ", Toast.LENGTH_SHORT).show();
//                        }
//                    });
            }

            @Override
            public void packetsAboveMinimumThreshold(int packetCount) {
                WOWZLog.debug("Packets have risen above threshold "+packetCount+" ... ");

//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(activity, "Packets have risen above threshold ... ", Toast.LENGTH_SHORT).show();
//                        }
//                    });
            }
        };
        mStreamPlayerView.setShowAllNotificationsWhenBelowThreshold(false);
        mStreamPlayerView.setMinimumPacketThreshold(20);
        mStreamPlayerView.registerPacketThresholdListener(packetChangeListener);
        ///// End packet change notification listener



        // listen for volume changes from device buttons, etc.
        mVolumeSettingChangeObserver = new VolumeChangeObserver(context, new Handler());
        context.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mVolumeSettingChangeObserver);
        mVolumeSettingChangeObserver.setVolumeChangeListener(new VolumeChangeObserver.VolumeChangeListener() {
            @Override
            public void onVolumeChanged(int previousLevel, int currentLevel) {

                if (mStreamPlayerView != null && mStreamPlayerView.getCurrentStatus().isPlaying()) {
                    mStreamPlayerView.setVolume(currentLevel);
                }
            }
        });



        // The streaming player configuration properties
        mStreamPlayerConfig = new WOWZPlayerConfig();
        // connection settings
        mStreamPlayerConfig.setHostAddress(host);
        mStreamPlayerConfig.setPortNumber(port);
        //streamConfig.setUseSSL(sharedPrefs.getBoolean("wz_live_use_ssl", false));
        mStreamPlayerConfig.setApplicationName(appName);
        mStreamPlayerConfig.setStreamName(streamName);
        mStreamPlayerConfig.setUsername(userName);
        mStreamPlayerConfig.setPassword(password);
        mStreamPlayerConfig.setIsPlayback(true);



        mBufferingDialog = new ProgressDialog(context);
        mBufferingDialog.setTitle("Retrieving Stream");
        mBufferingDialog.setMessage("Buffering stream ..");
        mBufferingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /// test

                cancelBuffering();
            }
        });

        mGoingDownDialog = new ProgressDialog(context);
        mGoingDownDialog.setTitle("Retrieving Stream");
        mGoingDownDialog.setMessage("Please wait while the decoder is shutting down.");

        mStreamPlayerView.registerDataEventListener("onClientConnected", new WOWZDataEvent.EventListener() {
            @Override
            public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                WOWZLog.info(TAG, "onClientConnected data event received:\n" + eventParams.toString(true));

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });

                // this demonstrates how to return a function result back to the original Wowza Streaming Engine
                // function call request
                WOWZDataMap functionResult = new WOWZDataMap();
                functionResult.put("greeting", "Hello New Client!");

                return functionResult;
            }
        });
        // testing player data event handler.
        mStreamPlayerView.registerDataEventListener("onWowzaData", new WOWZDataEvent.EventListener(){
            @Override
            public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                String meta = "";
                if(eventParams!=null)
                    meta = eventParams.toString();


                WOWZLog.debug("onWZDataEvent -> eventName "+eventName+" = "+meta);

                return null;
            }
        });

        // testing player data event handler.
        mStreamPlayerView.registerDataEventListener("onStatus", new WOWZDataEvent.EventListener(){
            @Override
            public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                if(eventParams!=null)
                    WOWZLog.debug("onWZDataEvent -> eventName "+eventName+" = "+eventParams.toString());

                return null;
            }
        });

        // testing player data event handler.
        mStreamPlayerView.registerDataEventListener("onTextData", new WOWZDataEvent.EventListener(){
            @Override
            public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                if(eventParams!=null)
                    WOWZLog.debug("onWZDataEvent -> "+eventName+" = "+eventParams.get("text"));

                return null;
            }
        });


    }

    @Override
    public View getView() {
        return mStreamPlayerView;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void onFlutterViewDetached() {
        if (mVolumeSettingChangeObserver != null)
            context.getContentResolver().unregisterContentObserver(mVolumeSettingChangeObserver);
    }

    private WOWZPlayerView getWowzaView(Context context) {
        WOWZPlayerView view = new WOWZPlayerView(context);
        return view;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "onResume":

            case "toggle":
                if (mStreamPlayerView.getCurrentStatus().isPlaying()) {
                    mStreamPlayerView.stop();
                } else if (mStreamPlayerView.isReadyToPlay()) {
                    playStream();
                }

                break;
            default:
                result.notImplemented();
        }

    }

    @Override
    public synchronized void onWZStatus(WOWZPlayerStatus status) {
        final WOWZPlayerStatus playerStatus = status;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                switch (playerStatus.getState()) {
                    case BUFFERING:
                        showBuffering();
                        break;
                    case CONNECTING:
                        showStartingDialog();
                        break;
                    case STOPPING:
                        hideBuffering();
                        showTearingdownDialog();
                        break;
                    case PLAYING:
                        hideBuffering();
                        break;

                    case IDLE:
                        if (playerStatus.getLastError() != null) {
                            displayErrorDialog(playerStatus.getLastError().toString());
                        }
                        playerStatus.clearLastError();

                        hideTearingdownDialog();
                        break;
                }
            }
        });
    }

    @Override
    public synchronized void onWZError(final WOWZPlayerStatus playerStatus) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                displayErrorDialog(playerStatus.getLastError().toString());
            }
        });
    }

    private void showStartingDialog(){
//
//
        try {
            if (mBufferingDialog == null) return;
//            hideBuffering();
            mBufferingDialog.setMessage("Please wait while we connect ..");
            if(!mBufferingDialog.isShowing()) {
                mBufferingDialog.setCancelable(false);
                mBufferingDialog.show();
            }
        }
        catch(Exception ex){
            WOWZLog.warn(TAG, "showTearingdownDialog:" + ex);
        }
    }

    private void showBuffering() {
        try {
            if (mBufferingDialog == null) return;

            if(mBufferingDialog.isShowing()){
                mBufferingDialog.setMessage("Buffering stream ...");
                return;
            }

            final Handler mainThreadHandler = new Handler(context.getMainLooper());
            mBufferingDialog.setCancelable(false);
            mBufferingDialog.show();
            mBufferingDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            (new Thread(){
                public void run(){

                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBufferingDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                        }
                    });
                }
            }).start();
        }
        catch(Exception ex) {
            WOWZLog.warn(TAG, "showBuffering:" + ex);
        }
    }


    public void playStream()
    {

        showBuffering();
        mStreamPlayerView.setMaxSecondsWithNoPackets(4);
        WOWZStreamingError configValidationError = mStreamPlayerConfig.validateForPlayback();
        if (configValidationError != null) {

        } else {
            // Start playback of the live stream
            mStreamPlayerView.play(mStreamPlayerConfig, this);
        }
    }
    private void showTearingdownDialog(){
        try {
            if (mGoingDownDialog == null) return;
            hideBuffering();
            if(!mGoingDownDialog.isShowing()) {
                mGoingDownDialog.setCancelable(false);
                mGoingDownDialog.show();
            }
        }
        catch(Exception ex){
            WOWZLog.warn(TAG, "showTearingdownDialog:" + ex);
        }
    }
    private void hideTearingdownDialog(){

        try {
            if (mGoingDownDialog == null) return;
            hideBuffering();
            mGoingDownDialog.dismiss();
        }
        catch(Exception ex){
            WOWZLog.warn(TAG, "hideTearingdownDialog exception:" + ex);
        }
    }

    private void cancelBuffering() {

        showTearingdownDialog();
        mStreamPlayerView.stop();
        hideTearingdownDialog();

    }

    private void hideBuffering() {
        if (mBufferingDialog!=null && mBufferingDialog.isShowing())
            mBufferingDialog.dismiss();
    }

    private void displayErrorDialog(String errorMessage) {
        // Log the error message
        try {
            WOWZLog.error(TAG, "ERROR: " + errorMessage);

            // Display an alert dialog containing the error message
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            //AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setMessage(errorMessage)
                    .setTitle("Error");
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
        catch(Exception ex){}
    }

}
