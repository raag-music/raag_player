#import "RaagPlayerPlugin.h"
#if __has_include(<raag_player/raag_player-Swift.h>)
#import <raag_player/raag_player-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "raag_player-Swift.h"
#endif

@implementation RaagPlayerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftRaagPlayerPlugin registerWithRegistrar:registrar];
}
@end
