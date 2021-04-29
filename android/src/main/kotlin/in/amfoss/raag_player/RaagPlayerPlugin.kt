package `in`.amfoss.raag_player

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.os.Build.ID
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException


/** RaagPlayerPlugin */

class RaagPlayerPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private lateinit var context: Context
  private lateinit var activity: Activity
  var mEqualizer: Equalizer? = null
  var messenger: BinaryMessenger? = null
  private var mediaPlayer: MediaPlayer? = null

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "raag_player")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
    messenger = flutterPluginBinding.binaryMessenger
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method){
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "initialize" -> {
        initialize()
      }
      "play" -> {
        play(call.argument<String>("url").toString())
      }
      "testToast" -> {
        Toast.makeText(context, "Raag Test", Toast.LENGTH_LONG).show()
      }
      else -> {
      result.notImplemented()
      }
    }
  }

  private fun initialize(){
    mediaPlayer = MediaPlayer()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      mediaPlayer?.setAudioAttributes(
              AudioAttributes
                      .Builder()
                      .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                      .build())
    }
    else mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

  }

  private fun initMediaPlayerIfNeeded() {
    if (mediaPlayer != null) {
      return
    }

    mediaPlayer = MediaPlayer().apply {
      setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
      setAudioStreamType(STREAM_MUSIC)
      setOnPreparedListener(mediaPlayer)
      setOnCompletionListener(mediaPlayer)
      setOnErrorListener(mediaPlayer)
    }
    setupEqualizer()
  }
  val Context.config: Config get() = Config.newInstance(context)

  private fun setupEqualizer() {
    try {
      mEqualizer = Equalizer(0, mediaPlayer!!.audioSessionId)
      if (!mEqualizer!!.enabled) {
        mEqualizer!!.enabled = true
      }
    } catch (e: Exception) {
    }
  }

  private fun play(url: String) {
    if (mediaPlayer == null) {
      try {
        mediaPlayer?.setDataSource(url)
      } catch (e: IOException) {
        Log.w(ID, "Invalid DataSource", e)
        channel.invokeMethod("audio.onError", "Invalid Datasource")
        return
      }
      mediaPlayer?.prepareAsync()
      mediaPlayer?.setOnPreparedListener {
        mediaPlayer?.start()
        channel.invokeMethod("audio.onStart", mediaPlayer?.duration)
      }
      mediaPlayer?.setOnCompletionListener {
        stop()
        channel.invokeMethod("audio.onComplete", null)
      }
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
