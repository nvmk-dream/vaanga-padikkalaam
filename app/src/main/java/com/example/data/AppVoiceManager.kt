package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import java.util.Locale

data class AppVoiceProfile(
    val id: String,
    val titleTamil: String,
    val titleEnglish: String,
    val toneDescription: String,
    val pitch: Float,
    val speed: Float,
    val styleNote: String
)

object AppVoiceManager {
    const val PREFS_NAME = "subiksha_voice_prefs"
    const val KEY_SELECTED_VOICE_ID = "selected_teacher_voice_id"

    // The two chosen voice profiles from user specifications
    val VOICE_CALM_GENTLE = AppVoiceProfile(
        id = "calm_gentle",
        titleTamil = "அமைதியான மென் குரல்",
        titleEnglish = "Calm & Gentle Teacher Voice",
        toneDescription = "பாசமாகப் பேசுதல் • இயல்பான பாணி",
        pitch = 1.15f,
        speed = 0.74f,
        styleNote = "சுருதி: 1.15x | வேகம்: 0.74x (கனிவான ஆசிரியை கற்பித்தல்)"
    )

    val VOICE_BOLD_HUSKY = AppVoiceProfile(
        id = "bold_husky",
        titleTamil = "தன்னம்பிக்கை கம்பீரக் குரல்",
        titleEnglish = "Bold & Confident Husky Voice",
        toneDescription = "ஆழமான குரல் • லேசான கரகரப்புடன் கூடிய கம்பீரக் குரல்",
        pitch = 0.74f,
        speed = 0.90f,
        styleNote = "சுருதி: 0.74x | வேகம்: 0.90x (கட்டையான அழுத்தமான ஆசிரியை குரல்)"
    )

    val ALL_VOICES = listOf(VOICE_CALM_GENTLE, VOICE_BOLD_HUSKY)

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedVoice(context: Context): AppVoiceProfile {
        val prefs = getPrefs(context)
        val savedId = prefs.getString(KEY_SELECTED_VOICE_ID, VOICE_CALM_GENTLE.id)
        return ALL_VOICES.firstOrNull { it.id == savedId } ?: VOICE_CALM_GENTLE
    }

    fun setSelectedVoice(context: Context, voiceId: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_SELECTED_VOICE_ID, voiceId).apply()
    }

    /**
     * Applies the selected voice settings (Pitch & SpeechRate) and female voice to any TextToSpeech engine.
     */
    fun configureTts(
        tts: TextToSpeech?,
        context: Context,
        isTamil: Boolean = false,
        overrideProfile: AppVoiceProfile? = null
    ) {
        if (tts == null) return
        val profile = overrideProfile ?: getSelectedVoice(context)

        if (isTamil) {
            val inTaLocale = Locale("ta", "IN")
            val result = tts.setLanguage(inTaLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                try {
                    tts.language = Locale("ta")
                } catch (_: Exception) {}
            }
            tts.setPitch(profile.pitch)
            tts.setSpeechRate(profile.speed)
        } else {
            tts.language = Locale.US
            try {
                val availableVoices = tts.voices
                val femaleVoice = availableVoices?.firstOrNull { v ->
                    val name = v.name.lowercase()
                    (v.locale.language == "en") &&
                    (name.contains("female") || name.contains("en-us-x-sfg") || name.contains("en-us-x-tpf") || name.contains("en-us-x-iol") || name.contains("#female"))
                } ?: availableVoices?.firstOrNull { v ->
                    v.locale.language == "en" && !v.name.lowercase().contains("male")
                }
                if (femaleVoice != null) {
                    tts.voice = femaleVoice
                }
            } catch (_: Exception) {}
            tts.setPitch(profile.pitch)
            tts.setSpeechRate(profile.speed)
        }
    }
}
