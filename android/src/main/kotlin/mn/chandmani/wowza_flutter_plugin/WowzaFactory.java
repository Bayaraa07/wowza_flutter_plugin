package mn.chandmani.wowza_flutter_plugin;

import android.content.Context;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class WowzaFactory extends PlatformViewFactory {
    private final BinaryMessenger messenger;
    private final Context mContext;

    public WowzaFactory(BinaryMessenger messenger,Context context) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
        this.mContext = context;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        if(Utils.hasArgument(args,"broadcast")) {
            boolean broadcast = Utils.argument(args,"broadcast");
            if(broadcast){
                return  new FlutterWowzaBroadcast(mContext, args, this.messenger, viewId);
            }
        }
        return new FlutterWowzaPlayer(mContext, args, this.messenger, viewId);
    }
}