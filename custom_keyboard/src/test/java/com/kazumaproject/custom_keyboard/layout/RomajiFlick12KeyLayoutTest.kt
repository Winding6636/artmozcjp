package com.kazumaproject.custom_keyboard.layout

import com.kazumaproject.custom_keyboard.data.FlickAction
import com.kazumaproject.custom_keyboard.data.FlickDirection
import com.kazumaproject.custom_keyboard.data.KeyAction
import com.kazumaproject.custom_keyboard.data.KeyData
import com.kazumaproject.custom_keyboard.data.KeyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RomajiFlick12KeyLayoutTest {

    private val layout = RomajiFlick12KeyLayout.create()

    private fun outputs(label: String): Map<FlickDirection, String> =
        layout.flickKeyMaps.getValue(label).single().mapValues { (_, action) ->
            (action as FlickAction.Input).char
        }

    private fun keyAt(row: Int, column: Int): KeyData =
        layout.keys.single { it.row == row && it.column == column }

    @Test
    fun layoutIsFiveColumnsByFourRowsInRomajiMode() {
        assertEquals(5, layout.columnCount)
        assertEquals(4, layout.rowCount)
        assertTrue(layout.isRomaji)
    }

    @Test
    fun twelveFlickKeysAreArrangedInThreeColumnsByFourRows() {
        val labels = (0..3).map { row -> (1..3).map { column -> keyAt(row, column).label } }
        assertEquals(
            listOf(
                listOf("A", "K", "H"),
                listOf("Y", "S", "T"),
                listOf("I", "N", "R"),
                listOf("U", "E", "O"),
            ),
            labels,
        )
        assertTrue(labels.flatten().all { label ->
            layout.keys.single { it.label == label }.keyType == KeyType.STANDARD_FLICK
        })
    }

    @Test
    fun tapOutputsTheLowercaseOfTheKeyLabel() {
        layout.keys.filter { it.keyType == KeyType.STANDARD_FLICK }.forEach { key ->
            assertEquals(key.label.lowercase(), outputs(key.label).getValue(FlickDirection.TAP))
        }
    }

    @Test
    fun upperRowsPlaceAuxiliaryLettersAroundTheMainLetter() {
        assertEquals(
            mapOf(
                FlickDirection.TAP to "a",
                FlickDirection.UP to "x",
                FlickDirection.UP_LEFT_FAR to "c",
            ),
            outputs("A"),
        )
        assertEquals(
            mapOf(
                FlickDirection.TAP to "k",
                FlickDirection.UP to "f",
                FlickDirection.UP_RIGHT_FAR to "g",
            ),
            outputs("K"),
        )
        assertEquals(
            mapOf(
                FlickDirection.TAP to "h",
                FlickDirection.UP to "p",
                FlickDirection.UP_RIGHT_FAR to "b",
            ),
            outputs("H"),
        )
    }

    @Test
    fun middleRowsHaveOneSideLetter() {
        assertEquals(mapOf(FlickDirection.TAP to "y", FlickDirection.UP_LEFT_FAR to "l"), outputs("Y"))
        assertEquals(mapOf(FlickDirection.TAP to "s", FlickDirection.UP_RIGHT_FAR to "z"), outputs("S"))
        assertEquals(mapOf(FlickDirection.TAP to "t", FlickDirection.UP_RIGHT_FAR to "d"), outputs("T"))
        assertEquals(mapOf(FlickDirection.TAP to "i", FlickDirection.UP_LEFT_FAR to "q"), outputs("I"))
        assertEquals(mapOf(FlickDirection.TAP to "n", FlickDirection.UP_RIGHT_FAR to "m"), outputs("N"))
        assertEquals(
            mapOf(
                FlickDirection.TAP to "r",
                FlickDirection.UP to "j",
                FlickDirection.UP_RIGHT_FAR to "w",
            ),
            outputs("R"),
        )
    }

    @Test
    fun bottomRowCarriesPunctuationInAllFourDirections() {
        assertEquals(
            mapOf(
                FlickDirection.TAP to "u",
                FlickDirection.UP_LEFT_FAR to "v",
                FlickDirection.UP_RIGHT_FAR to "。",
                FlickDirection.DOWN to "~",
            ),
            outputs("U"),
        )
        assertEquals(
            mapOf(
                FlickDirection.TAP to "e",
                FlickDirection.UP_LEFT_FAR to "!",
                FlickDirection.UP_RIGHT_FAR to "、",
                FlickDirection.DOWN to "(",
            ),
            outputs("E"),
        )
        assertEquals(
            mapOf(
                FlickDirection.TAP to "o",
                FlickDirection.UP_LEFT_FAR to "?",
                FlickDirection.UP_RIGHT_FAR to "-",
                FlickDirection.DOWN to ")",
            ),
            outputs("O"),
        )
    }

    @Test
    fun everyAlphabetLetterIsReachableExactlyOnce() {
        val letters = layout.keys.filter { it.keyType == KeyType.STANDARD_FLICK }.flatMap { outputs(it.label).values }
            .filter { it.length == 1 && it[0] in 'a'..'z' }
        assertEquals(('a'..'z').map { it.toString() }, letters.sorted())
    }

    private fun actions(l: com.kazumaproject.custom_keyboard.data.KeyboardLayout, keyId: String) =
        l.flickKeyMaps.getValue(keyId).single().mapValues { (_, a) -> (a as FlickAction.Action).action }

    @Test
    fun leftColumnIsEmojiCursorNumberAndModeKey() {
        assertEquals(KeyAction.ShowEmojiKeyboard, keyAt(0, 0).action)
        assertEquals(KeyAction.MoveCursorLeft, keyAt(1, 0).action)
        assertEquals(KeyAction.SwitchToNumberLayout, keyAt(2, 0).action)
        assertEquals(RomajiFlick12KeyLayout.MODE_KEY_ID, keyAt(3, 0).keyId)
        assertEquals(KeyType.CROSS_FLICK, keyAt(3, 0).keyType)
    }

    @Test
    fun modeKeyCyclesOnTapAndJumpsToTheOtherLanguageOnUp() {
        assertEquals(
            mapOf(
                FlickDirection.TAP to KeyAction.SwitchRomajiEnglish,
                FlickDirection.UP to KeyAction.SwitchToEnglishLayout,
                FlickDirection.UP_RIGHT_FAR to KeyAction.SwitchToNextIme,
                FlickDirection.UP_LEFT_FAR to KeyAction.ShowEmojiKeyboard,
            ),
            actions(layout, RomajiFlick12KeyLayout.MODE_KEY_ID),
        )
        val english = RomajiFlick12KeyLayout.create(isRomaji = false)
        assertEquals(
            KeyAction.SwitchToKanaLayout,
            actions(english, RomajiFlick12KeyLayout.MODE_KEY_ID).getValue(FlickDirection.UP),
        )
    }

    @Test
    fun englishLayoutPutsShiftWhereJapaneseHasEmoji() {
        val english = RomajiFlick12KeyLayout.create(isRomaji = false)
        assertEquals(KeyAction.ShiftKey, english.keys.single { it.row == 0 && it.column == 0 }.action)
        assertEquals(KeyAction.ShowEmojiKeyboard, keyAt(0, 0).action)
    }

    @Test
    fun modeKeyLabelListsAllThreeTargetsAndEnlargesTheCurrentLanguage() {
        assertEquals("あa\uD83C\uDF10", keyAt(3, 0).label)
        val english = RomajiFlick12KeyLayout.create(isRomaji = false)
        assertEquals("ぁA\uD83C\uDF10", english.keys.single { it.row == 3 && it.column == 0 }.label)
        assertEquals(false, english.isRomaji)
    }

    @Test
    fun englishModeUsesAsciiPeriodAndCommaInsteadOfJapanesePunctuation() {
        val english = RomajiFlick12KeyLayout.create(isRomaji = false)
        fun right(label: String) =
            (english.flickKeyMaps.getValue(label).single().getValue(FlickDirection.UP_RIGHT_FAR) as FlickAction.Input).char
        assertEquals(".", right("U"))
        assertEquals(",", right("E"))
        assertEquals("-", right("O"))
    }

    @Test
    fun spaceKeyEntersHalfWidthOnUpFullWidthOnLeftAndConvertsToKatakanaOnDown() {
        assertEquals(RomajiFlick12KeyLayout.SPACE_KEY_ID, keyAt(2, 4).keyId)
        assertEquals(
            mapOf(
                FlickDirection.TAP to KeyAction.Space,
                FlickDirection.UP to KeyAction.ForceHalfWidthSpace,
                FlickDirection.UP_LEFT_FAR to KeyAction.ForceFullWidthSpace,
                FlickDirection.DOWN to KeyAction.ToggleKatakana,
            ),
            actions(layout, RomajiFlick12KeyLayout.SPACE_KEY_ID),
        )
        assertEquals(
            listOf(KeyAction.Delete, KeyAction.MoveCursorRight, null, KeyAction.Enter),
            (0..3).map { keyAt(it, 4).action },
        )
    }

    @Test
    fun numberLayoutFollowsTheOriginalArrangementWithABottomBar() {
        val number = RomajiFlick12KeyLayout.createNumber()
        assertEquals(5, number.rowCount)
        fun at(row: Int, column: Int) = number.keys.single { it.row == row && it.column == column }
        assertEquals(
            listOf(
                listOf("+", "1", "2", "3"),
                listOf("-", "4", "5", "6"),
                listOf(",", "7", "8", "9"),
                listOf(".", "*", "0", "#"),
            ),
            (0..3).map { row -> (0..3).map { column -> at(row, column).label } },
        )
        assertEquals(listOf("/", "="), (1..2).map { at(it, 4).label })
        assertEquals(KeyAction.Delete, at(0, 4).action)
        assertEquals(RomajiFlick12KeyLayout.SPACE_KEY_ID, at(3, 4).keyId)
        number.keys.filter { it.keyType == KeyType.STANDARD_FLICK }.forEach { key ->
            val tap = number.flickKeyMaps.getValue(key.label).single().getValue(FlickDirection.TAP)
            assertEquals(key.label, (tap as FlickAction.Input).char)
        }
    }

    @Test
    fun numberBottomBarHasBackEmojiGlobeAndEnter() {
        val number = RomajiFlick12KeyLayout.createNumber()
        assertEquals(
            listOf(
                KeyAction.SwitchToKanaLayout,
                KeyAction.DoNothing,
                KeyAction.ShowEmojiKeyboard,
                KeyAction.SwitchToNextIme,
                KeyAction.Enter,
            ),
            (0..4).map { column -> number.keys.single { it.row == 4 && it.column == column }.action },
        )
    }
}
