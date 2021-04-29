package `in`.amfoss.raag_player


import android.content.Context

class Config(context: Context) {
    private val prefs = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var equalizerPreset: Int
        get() = prefs.getInt("EQUALIZER_PRESET", 0)
        set(equalizerPreset) = prefs.edit().putInt("EQUALIZER_PRESET", equalizerPreset).apply()

    var equalizerBands: String
        get() = prefs.getString("EQUALIZER_BANDS", "")!!
        set(equalizerBands) = prefs.edit().putString("EQUALIZER_BANDS", equalizerBands).apply()

}
