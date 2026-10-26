package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class TamilMathSpeaker(
    private val context: Context,
    private val onInitComplete: (Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isReady = false
        private set

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val tamilLocale = Locale("ta", "IN")
            val result = tts?.setLanguage(tamilLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if Tamil voice data not installed on device
                tts?.setLanguage(Locale.ENGLISH)
            }

            // Configure pleasant, clear Female Voice characteristics
            tts?.setPitch(1.18f)
            tts?.setSpeechRate(0.92f)

            // Try selecting available female voice from device TTS engine
            try {
                val voices = tts?.voices
                if (voices != null) {
                    val femaleVoice = voices.firstOrNull { voice ->
                        (voice.locale.language == "ta" || voice.locale.language == "en") &&
                                (voice.name.contains("female", ignoreCase = true) ||
                                        voice.name.contains("f0", ignoreCase = true) ||
                                        voice.name.contains("woman", ignoreCase = true))
                    }
                    if (femaleVoice != null) {
                        tts?.voice = femaleVoice
                    }
                }
            } catch (_: Exception) {}

            isReady = true
            onInitComplete(true)
        } else {
            isReady = false
            onInitComplete(false)
        }
    }

    fun speak(text: String, onComplete: () -> Unit = {}) {
        if (!isReady || text.isBlank()) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "math_tts_id")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
