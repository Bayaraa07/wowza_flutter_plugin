#import "WowzaFlutterPlugin.h"
#if __has_include(<wowza_flutter_plugin/wowza_flutter_plugin-Swift.h>)
#import <wowza_flutter_plugin/wowza_flutter_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "wowza_flutter_plugin-Swift.h"
#endif

@implementation WowzaFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftWowzaFlutterPlugin registerWithRegistrar:registrar];
}
@end
