package com.kazumaproject.markdownhelperkeyboard.ime_service

import com.kazumaproject.custom_keyboard.data.KeyboardInputMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardGuideVisibilityTest {

    private val sumireSettings = ModeKeymapGuideSettings(
        japanese = true,
        english = false,
        number = true
    )

    @Test
    fun sumireSurface_selectsSettingForEachInputMode() {
        val japanese = resolveFlickGuideRuntimeConfig(
            FlickGuideSurface.Sumire,
            KeyboardInputMode.HIRAGANA,
            sumireSettings,
            customGuideEnabled = false
        )
        val english = resolveFlickGuideRuntimeConfig(
            FlickGuideSurface.Sumire,
            KeyboardInputMode.ENGLISH,
            sumireSettings,
            customGuideEnabled = true
        )
        val number = resolveFlickGuideRuntimeConfig(
            FlickGuideSurface.Sumire,
            KeyboardInputMode.SYMBOLS,
            sumireSettings,
            customGuideEnabled = false
        )

        assertTrue(japanese.enabled)
        assertFalse(english.enabled)
        assertTrue(number.enabled)
        assertTrue(japanese.allowMultiCharacterLabels)
        assertTrue(english.allowMultiCharacterLabels)
        assertTrue(number.allowMultiCharacterLabels)
    }

    @Test
    fun customSurface_keepsLegacyCustomSettingAndLabelPolicy() {
        val config = resolveFlickGuideRuntimeConfig(
            FlickGuideSurface.Custom,
            KeyboardInputMode.ENGLISH,
            sumireSettings,
            customGuideEnabled = true
        )

        assertTrue(config.enabled)
        assertFalse(config.allowMultiCharacterLabels)
    }

    @Test
    fun romajiFlick12Surface_showsGuideByDefaultRegardlessOfCustomSetting() {
        val config = resolveFlickGuideRuntimeConfig(
            FlickGuideSurface.RomajiFlick12,
            KeyboardInputMode.HIRAGANA,
            sumireSettings,
            customGuideEnabled = false
        )

        assertTrue(config.enabled)
        assertFalse(config.allowMultiCharacterLabels)
    }

    @Test
    fun romajiFlick12Surface_hidesGuideWhenItsOwnSettingIsOff() {
        val config = resolveFlickGuideRuntimeConfig(
            FlickGuideSurface.RomajiFlick12,
            KeyboardInputMode.HIRAGANA,
            sumireSettings,
            customGuideEnabled = true,
            romajiFlick12GuideEnabled = false
        )

        assertFalse(config.enabled)
    }

    @Test
    fun qwertyOrOtherSurface_neverAppliesSumireGuide() {
        KeyboardInputMode.entries.forEach { mode ->
            assertEquals(
                FlickGuideRuntimeConfig(
                    enabled = false,
                    allowMultiCharacterLabels = false
                ),
                resolveFlickGuideRuntimeConfig(
                    FlickGuideSurface.Other,
                    mode,
                    ModeKeymapGuideSettings(true, true, true),
                    customGuideEnabled = true
                )
            )
        }
    }
}
