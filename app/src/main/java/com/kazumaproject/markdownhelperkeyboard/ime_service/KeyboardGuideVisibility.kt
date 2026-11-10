package com.kazumaproject.markdownhelperkeyboard.ime_service

import com.kazumaproject.custom_keyboard.data.KeyboardInputMode

internal data class ModeKeymapGuideSettings(
    val japanese: Boolean = false,
    val english: Boolean = false,
    val number: Boolean = false
) {
    fun isEnabled(mode: KeyboardInputMode): Boolean {
        return when (mode) {
            KeyboardInputMode.HIRAGANA -> japanese
            KeyboardInputMode.ENGLISH -> english
            KeyboardInputMode.SYMBOLS -> number
        }
    }
}

internal enum class FlickGuideSurface {
    Sumire,
    Custom,
    RomajiFlick12,
    Other
}

internal data class FlickGuideRuntimeConfig(
    val enabled: Boolean,
    val allowMultiCharacterLabels: Boolean
)

internal fun resolveFlickGuideRuntimeConfig(
    surface: FlickGuideSurface,
    mode: KeyboardInputMode,
    sumireSettings: ModeKeymapGuideSettings,
    customGuideEnabled: Boolean,
    romajiFlick12GuideEnabled: Boolean = true
): FlickGuideRuntimeConfig {
    return when (surface) {
        FlickGuideSurface.Sumire -> FlickGuideRuntimeConfig(
            enabled = sumireSettings.isEnabled(mode),
            allowMultiCharacterLabels = true
        )

        FlickGuideSurface.Custom -> FlickGuideRuntimeConfig(
            enabled = customGuideEnabled,
            allowMultiCharacterLabels = false
        )

        FlickGuideSurface.RomajiFlick12 -> FlickGuideRuntimeConfig(
            enabled = romajiFlick12GuideEnabled,
            allowMultiCharacterLabels = false
        )

        FlickGuideSurface.Other -> FlickGuideRuntimeConfig(
            enabled = false,
            allowMultiCharacterLabels = false
        )
    }
}
