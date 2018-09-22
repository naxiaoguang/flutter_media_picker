import 'dart:async';

import 'package:flutter/services.dart';

class MediaPicker {
  static const MethodChannel _channel =
      const MethodChannel('media_picker');

  static Future<String> pick(int maxCount,bool allowPickVideo) async {
    final String version = await _channel.invokeMethod('pick',{maxCount:maxCount,allowPickVideo:allowPickVideo});
    return version;
  }
}
