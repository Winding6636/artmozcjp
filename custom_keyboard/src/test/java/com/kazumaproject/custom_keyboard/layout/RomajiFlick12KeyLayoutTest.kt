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
        val letters = layout.flickKeyMaps.keys.flatMap { label -> outputs(label).values }
            .filter { it.length == 1 && it[0] in 'a'..'z' }
        assertEquals(('a'..'z').map { it.toString() }, letters.sorted())
    }

    @Test
    fun sideColumnsHoldTheEditingKeys() {
        assertEquals(
            listOf(
                KeyAction.SwitchToNextIme,
                KeyAction.MoveCursorLeft,
                KeyAction.ShowEmojiKeyboard,
                KeyAction.SwitchRomajiEnglish,
            ),
            (0..3).map { keyAt(it, 0).action },
        )
        assertEquals(
            listOf(KeyAction.Delete, KeyAction.MoveCursorRight, KeyAction.Space, KeyAction.Enter),
            (0..3).map { keyAt(it, 4).action },
        )
    }
}
