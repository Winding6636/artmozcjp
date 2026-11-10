package com.kazumaproject.custom_keyboard.layout

import com.kazumaproject.custom_keyboard.data.FlickAction
import com.kazumaproject.custom_keyboard.data.FlickDirection
import com.kazumaproject.custom_keyboard.data.KeyAction
import com.kazumaproject.custom_keyboard.data.KeyData
import com.kazumaproject.custom_keyboard.data.KeyType
import com.kazumaproject.custom_keyboard.data.KeyboardLayout

// かなを直接出力するキーは置かない。変換エンジンの手前をローマ字入力に統一して romaji モードに任せる。
object RomajiFlick12KeyLayout {

    private data class FlickKeySpec(
        val row: Int,
        val column: Int,
        val center: String,
        val up: String? = null,
        val left: String? = null,
        val right: String? = null,
        val down: String? = null,
    )

    private val flickKeySpecs = listOf(
        FlickKeySpec(0, 1, "a", up = "x", left = "c"),
        FlickKeySpec(0, 2, "k", up = "f", right = "g"),
        FlickKeySpec(0, 3, "h", up = "p", right = "b"),
        FlickKeySpec(1, 1, "y", left = "l"),
        FlickKeySpec(1, 2, "s", right = "z"),
        FlickKeySpec(1, 3, "t", right = "d"),
        FlickKeySpec(2, 1, "i", left = "q"),
        FlickKeySpec(2, 2, "n", right = "m"),
        FlickKeySpec(2, 3, "r", up = "j", right = "w"),
        FlickKeySpec(3, 1, "u", left = "v", right = "。", down = "~"),
        FlickKeySpec(3, 2, "e", left = "!", right = "、", down = "("),
        FlickKeySpec(3, 3, "o", left = "?", right = "-", down = ")"),
    )

    const val MODE_KEY_ID = "romaji_flick12_mode"
    const val SPACE_KEY_ID = "romaji_flick12_space"

    private val digitRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(",", "0", "."),
    )

    fun create(isRomaji: Boolean = true): KeyboardLayout {
        val leftColumn = listOf(
            specialKey(
                row = 0, column = 0, action = KeyAction.ShowEmojiKeyboard,
                drawable = com.kazumaproject.core.R.drawable.baseline_emoji_emotions_24,
            ),
            cursorLeftKey(),
            KeyData(
                label = "123", row = 2, column = 0, isFlickable = false,
                action = KeyAction.SwitchToNumberLayout, isSpecialKey = true,
            ),
            KeyData(
                label = if (isRomaji) "あ" else "A", row = 3, column = 0, isFlickable = false,
                isSpecialKey = true, keyId = MODE_KEY_ID, keyType = KeyType.CROSS_FLICK,
            ),
        )

        val flickKeys = flickKeySpecs.map { spec ->
            KeyData(
                label = spec.center.uppercase(),
                row = spec.row,
                column = spec.column,
                isFlickable = true,
                keyType = KeyType.STANDARD_FLICK,
            )
        }

        val flickMaps = flickKeySpecs.associate { spec ->
            spec.center.uppercase() to listOf(spec.toFlickMap())
        } + mapOf(
            MODE_KEY_ID to listOf(
                mapOf(
                    FlickDirection.TAP to FlickAction.Action(KeyAction.SwitchRomajiEnglish),
                    FlickDirection.UP to FlickAction.Action(KeyAction.SwitchToEnglishLayout, label = "A"),
                    FlickDirection.UP_RIGHT_FAR to FlickAction.Action(
                        KeyAction.SwitchToNextIme,
                        drawableResId = com.kazumaproject.core.R.drawable.language_24dp,
                    ),
                    FlickDirection.DOWN to FlickAction.Action(KeyAction.SwitchToKanaLayout, label = "あ"),
                )
            ),
            SPACE_KEY_ID to listOf(spaceFlickMap()),
        )

        return KeyboardLayout(
            keys = leftColumn + rightColumn() + flickKeys,
            flickKeyMaps = flickMaps,
            columnCount = 5,
            rowCount = 4,
            isRomaji = isRomaji,
        )
    }

    fun createNumber(): KeyboardLayout {
        val leftColumn = listOf(
            specialKey(
                row = 0, column = 0, action = KeyAction.ShowEmojiKeyboard,
                drawable = com.kazumaproject.core.R.drawable.baseline_emoji_emotions_24,
            ),
            cursorLeftKey(),
            KeyData(
                label = "あA", row = 2, column = 0, isFlickable = false,
                action = KeyAction.SwitchToKanaLayout, isSpecialKey = true,
            ),
            specialKey(
                row = 3, column = 0, action = KeyAction.SwitchToNextIme,
                drawable = com.kazumaproject.core.R.drawable.language_24dp,
                keyId = "switch_next_ime",
            ),
        )

        val digitKeys = digitRows.flatMapIndexed { row, labels ->
            labels.mapIndexed { index, label ->
                KeyData(
                    label = label, row = row, column = index + 1, isFlickable = true,
                    keyType = KeyType.STANDARD_FLICK,
                )
            }
        }

        val flickMaps = digitRows.flatten().associateWith { label ->
            listOf(mapOf<FlickDirection, FlickAction>(FlickDirection.TAP to FlickAction.Input(label)))
        } + mapOf(SPACE_KEY_ID to listOf(spaceFlickMap()))

        return KeyboardLayout(
            keys = leftColumn + rightColumn() + digitKeys,
            flickKeyMaps = flickMaps,
            columnCount = 5,
            rowCount = 4,
            isRomaji = true,
        )
    }

    private fun cursorLeftKey() = specialKey(
        row = 1, column = 0, action = KeyAction.MoveCursorLeft,
        drawable = com.kazumaproject.core.R.drawable.baseline_arrow_left_24,
    )

    private fun rightColumn() = listOf(
        specialKey(
            row = 0, column = 4, action = KeyAction.Delete,
            drawable = com.kazumaproject.core.R.drawable.backspace_24px,
        ),
        specialKey(
            row = 1, column = 4, action = KeyAction.MoveCursorRight,
            drawable = com.kazumaproject.core.R.drawable.baseline_arrow_right_24,
        ),
        KeyData(
            label = "", row = 2, column = 4, isFlickable = false, isSpecialKey = true,
            drawableResId = com.kazumaproject.core.R.drawable.baseline_space_bar_24,
            keyId = SPACE_KEY_ID, keyType = KeyType.CROSS_FLICK,
        ),
        specialKey(
            row = 3, column = 4, action = KeyAction.Enter,
            drawable = com.kazumaproject.core.R.drawable.baseline_keyboard_return_24,
        ),
    )

    private fun spaceFlickMap(): Map<FlickDirection, FlickAction> = mapOf(
        FlickDirection.TAP to FlickAction.Action(KeyAction.Space),
        FlickDirection.UP to FlickAction.Action(KeyAction.ForceFullWidthSpace, label = "全角"),
        FlickDirection.DOWN to FlickAction.Action(KeyAction.ForceHalfWidthSpace, label = "半角"),
    )

    private fun specialKey(
        row: Int,
        column: Int,
        action: KeyAction,
        drawable: Int,
        keyId: String? = null,
    ) = KeyData(
        label = "",
        row = row,
        column = column,
        isFlickable = false,
        action = action,
        isSpecialKey = true,
        drawableResId = drawable,
        keyId = keyId,
    )

    private fun FlickKeySpec.toFlickMap(): Map<FlickDirection, FlickAction> =
        buildMap {
            put(FlickDirection.TAP, input(center))
            up?.let { put(FlickDirection.UP, input(it)) }
            left?.let { put(FlickDirection.UP_LEFT_FAR, input(it)) }
            right?.let { put(FlickDirection.UP_RIGHT_FAR, input(it)) }
            down?.let { put(FlickDirection.DOWN, input(it)) }
        }

    private fun input(text: String): FlickAction =
        FlickAction.Input(text, label = text.uppercase())
}
