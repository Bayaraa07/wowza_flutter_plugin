package mn.chandmani.wowza_flutter_plugin

import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.PluginRegistry.Registrar

/** WowzaFlutterPlugin */
public class WowzaFlutterPlugin: FlutterPlugin {

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    Log.d("WowzaFlutter","onAttachedToEngine")
    flutterPluginBinding.platformViewRegistry.registerViewFactory(
            "wowza_flutter_plugin",  WowzaFactory(flutterPluginBinding.binaryMessenger,flutterPluginBinding.applicationContext ))
    
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      registrar
              .platformViewRegistry()
              .registerViewFactory(
                      "wowza_flutter_plugin",  WowzaFactory(registrar.messenger(),registrar.context()))
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }
}
