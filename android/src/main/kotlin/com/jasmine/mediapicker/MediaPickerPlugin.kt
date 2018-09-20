package com.jasmine.mediapicker

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class MediaPickerPlugin private constructor(private var registrar: PluginRegistry.Registrar,private val delegate:MediaPickerDelegate) : MethodCallHandler {
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "media_picker")
      val delegate = MediaPickerDelegate(registrar.activity())
      val plugin = MediaPickerPlugin(registrar,delegate)
      registrar.addActivityResultListener(delegate)
      channel.setMethodCallHandler(plugin)
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (registrar.activity() == null) {
      result.error("no_activity", "image_picker plugin requires a foreground activity.", null)
      return
    }
    when {
      call.method == "pick" -> {
        delegate.initPicker(call,result)
      }
      else -> result.notImplemented()
    }
  }
}
