package `in`.amfoss.raag_player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build.ID
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException


/** RaagPlayerPlugin */

class RaagPlayerPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private var context: Context? = null
  var messenger: BinaryMessenger? = null
  var activity: Activity? = null
  var mediaPlayer: MediaPlayer? = null
  
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "raag_player")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
    messenger = flutterPluginBinding.binaryMessenger
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method){
      "testToast" -> {
        Toast.makeText(context, "Raag Test", Toast.LENGTH_LONG).show()
      }
      "play" -> {
        play(call.argument<String>("url").toString())
      }
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      else -> {
      result.notImplemented()
      }
    }
  }

  private fun play(url: String) {
    if (mediaPlayer == null) {
      mediaPlayer = MediaPlayer()
      mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
      try {
        mediaPlayer?.setDataSource(url)
      } catch (e: IOException) {
        Log.w(ID, "Invalid DataSource", e)
        channel.invokeMethod("audio.onError", "Invalid Datasource")
        return
      }
      mediaPlayer?.prepareAsync()
      mediaPlayer?.setOnPreparedListener(OnPreparedListener {
        mediaPlayer?.start()
        channel.invokeMethod("audio.onStart", mediaPlayer?.duration)
      })
      mediaPlayer?.setOnCompletionListener(OnCompletionListener {
        stop()
        channel.invokeMethod("audio.onComplete", null)
      })
      mediaPlayer?.setOnErrorListener(MediaPlayer.OnErrorListener { mp, what, extra ->
        channel.invokeMethod("audio.onError", String.format("{\"what\":%d,\"extra\":%d}", what, extra))
        true
      })
    } else {
      mediaPlayer?.start()
      channel.invokeMethod("audio.onStart", mediaPlayer?.duration)
    }
  }

  private fun stop(){

  }
  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    mediaPlayer?.stop()
  }
}
