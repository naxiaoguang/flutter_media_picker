import 'dart:async';

import 'package:flutter/services.dart';

class MediaPicker {
  static const MethodChannel _channel =
      const MethodChannel('media_picker');

  static Future<String> get pick async {
    final String version = await _channel.invokeMethod('pick');
    return version;
  }
}
