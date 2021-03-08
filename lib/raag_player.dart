
import 'dart:async';

import 'package:flutter/services.dart';

class RaagPlayer {
  static const MethodChannel _channel =
      const MethodChannel('raag_player');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future get testToast async{
    await _channel.invokeMethod('testToast');
  }

  static Future play(String url) async{
    await _channel.invokeMethod('play', {'url': url});
  }

  static Future stop() async {
    await _channel.invokeMethod('stop');
}

}
