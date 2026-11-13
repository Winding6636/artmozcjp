package com.kazumaproject.custom_keyboard.layout

import com.kazumaproject.custom_keyboard.data.FlickAction
import com.kazumaproject.custom_keyboard.data.FlickDirection
import com.kazumaproject.custom_keyboard.data.KeyAction
import com.kazumaproject.custom_keyboard.data.KeyData
import com.kazumaproject.custom_keyboard.data.KeyType
import com.kazumaproject.custom_keyboard.data.KeyboardLayout

// かなを直接出力するキーは置かない。変換エンジンの手前をローマ字入力に統一して romaji モードに任せる。
enum class RomajiFlick12SpaceFlick { HALF_WIDTH_SPACE, FULL_WIDTH_SPACE, KATAKANA, NONE }

data class RomajiFlick12Options(
    val englishModeEnabled: Boolean = true,
    val spaceUp: RomajiFlick12SpaceFlick = RomajiFlick12SpaceFlick.HALF_WIDTH_SPACE,
    val spaceLeft: RomajiFlick12SpaceFlick = RomajiFlick12SpaceFlick.FULL_WIDTH_SPACE,
    val spaceDown: RomajiFlick12SpaceFlick = RomajiFlick12SpaceFlick.KATAKANA,
    val spaceRight: RomajiFlick12SpaceFlick = RomajiFlick12SpaceFlick.NONE,
)

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
    const val SPACE_LABEL = "\u2423"
    const val MODE_LABEL_JAPANESE = "あa\uD83C\uDF10"
    const val MODE_LABEL_JAPANESE_ONLY = "あ\uD83C\uDF10"
    const val MODE_LABEL_ENGLISH = "ぁA\uD83C\uDF10"

    private val numberRows = listOf(
        listOf("+", "1", "2", "3"),
        listOf("-", "4", "5", "6"),
        listOf(",", "7", "8", "9"),
        listOf(".", "*", "0", "#"),
    )

    private fun emojiKey(row: Int, column: Int) = specialKey(
        row = row, column = column, action = KeyAction.ShowEmojiKeyboard,
        drawable = com.kazumaproject.core.R.drawable.baseline_emoji_emotions_24,
    )

    private fun modeKeyFlickMap(isRomaji: Boolean, options: RomajiFlick12Options): Map<FlickDirection, FlickAction> = buildMap {
        put(FlickDirection.TAP, FlickAction.Action(KeyAction.SwitchRomajiEnglish))
        if (options.englishModeEnabled) {
            put(
                FlickDirection.UP,
                if (isRomaji) FlickAction.Action(KeyAction.SwitchToEnglishLayout, label = "A")
                else FlickAction.Action(KeyAction.SwitchToKanaLayout, label = "あ")
            )
        }
        putAll(modeKeyCornerMap())
    }

    private fun modeKeyCornerMap(): Map<FlickDirection, FlickAction> = mapOf(
        FlickDirection.UP_RIGHT_FAR to FlickAction.Action(
            KeyAction.SwitchToNextIme,
            drawableResId = com.kazumaproject.core.R.drawable.language_24dp,
        ),
        FlickDirection.UP_LEFT_FAR to FlickAction.Action(
            KeyAction.ShowEmojiKeyboard,
            drawableResId = com.kazumaproject.core.R.drawable.baseline_emoji_emotions_24,
        ),
    )

    fun create(isRomaji: Boolean = true, options: RomajiFlick12Options = RomajiFlick12Options()): KeyboardLayout {
        val topLeft = if (isRomaji) {
            emojiKey(row = 0, column = 0)
        } else {
            specialKey(
                row = 0, column = 0, action = KeyAction.ShiftKey,
                drawable = com.kazumaproject.core.R.drawable.shift_24px,
            )
        }
        val leftColumn = listOf(
            topLeft,
            cursorLeftKey(),
            KeyData(
                label = "123", row = 2, column = 0, isFlickable = false,
                action = KeyAction.SwitchToNumberLayout, isSpecialKey = true,
            ),
            KeyData(
                label = modeLabel(isRomaji, options), row = 3, column = 0, isFlickable = false,
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
            spec.center.uppercase() to listOf(spec.toFlickMap(isRomaji))
        } + mapOf(
            MODE_KEY_ID to listOf(modeKeyFlickMap(isRomaji, options)),
            SPACE_KEY_ID to listOf(spaceFlickMap(options)),
        )

        return KeyboardLayout(
            keys = leftColumn + rightColumn(spaceRow = 2, enterRow = 3) + flickKeys,
            flickKeyMaps = flickMaps,
            columnCount = 5,
            rowCount = 4,
            isRomaji = isRomaji,
            usesUniformGrid = true,
        )
    }

    fun createNumber(options: RomajiFlick12Options = RomajiFlick12Options()): KeyboardLayout {
        val symbolKeys = numberRows.flatMapIndexed { row, labels ->
            labels.mapIndexed { index, label ->
                KeyData(
                    label = label, row = row, column = index, isFlickable = true,
                    keyType = KeyType.STANDARD_FLICK,
                )
            }
        } + listOf("/", "=").mapIndexed { index, label ->
            KeyData(
                label = label, row = index + 1, column = 4, isFlickable = true,
                keyType = KeyType.STANDARD_FLICK,
            )
        }

        val bottomBar = listOf(
            KeyData(
                label = "あいう", row = 4, column = 0, isFlickable = false,
                action = KeyAction.SwitchToKanaLayout, isSpecialKey = true,
            ),
            KeyData(
                label = "123", row = 4, column = 1, isFlickable = false,
                action = KeyAction.DoNothing, isSpecialKey = true, isHiLighted = true,
            ),
            emojiKey(row = 4, column = 2),
            specialKey(
                row = 4, column = 3, action = KeyAction.SwitchToNextIme,
                drawable = com.kazumaproject.core.R.drawable.language_24dp,
                keyId = "switch_next_ime",
            ),
            specialKey(
                row = 4, column = 4, action = KeyAction.Enter,
                drawable = com.kazumaproject.core.R.drawable.baseline_keyboard_return_24,
            ),
        )

        val flickMaps = (numberRows.flatten() + listOf("/", "=")).associateWith { label ->
            listOf(mapOf<FlickDirection, FlickAction>(FlickDirection.TAP to FlickAction.Input(label)))
        } + mapOf(SPACE_KEY_ID to listOf(spaceFlickMap(options)))

        return KeyboardLayout(
            keys = rightColumn(spaceRow = 3, enterRow = null) + symbolKeys + bottomBar,
            flickKeyMaps = flickMaps,
            columnCount = 5,
            rowCount = 5,
            isRomaji = true,
            usesUniformGrid = true,
        )
    }

    private fun cursorLeftKey() = specialKey(
        row = 1, column = 0, action = KeyAction.MoveCursorLeft,
        drawable = com.kazumaproject.core.R.drawable.cursor_arrow_left_large,
    )

    private fun rightColumn(spaceRow: Int, enterRow: Int?): List<KeyData> = listOfNotNull(
        specialKey(
            row = 0, column = 4, action = KeyAction.Delete,
            drawable = com.kazumaproject.core.R.drawable.backspace_24px,
        ),
        if (enterRow != null) {
            specialKey(
                row = 1, column = 4, action = KeyAction.MoveCursorRight,
                drawable = com.kazumaproject.core.R.drawable.cursor_arrow_right_large,
            )
        } else {
            null
        },
        KeyData(
            label = SPACE_LABEL, row = spaceRow, column = 4, isFlickable = false, isSpecialKey = true,
            keyId = SPACE_KEY_ID, keyType = KeyType.CROSS_FLICK,
        ),
        if (enterRow != null) {
            specialKey(
                row = enterRow, column = 4, action = KeyAction.Enter,
                drawable = com.kazumaproject.core.R.drawable.baseline_keyboard_return_24,
            )
        } else {
            null
        },
    )

    private fun modeLabel(isRomaji: Boolean, options: RomajiFlick12Options): String = when {
        options.englishModeEnabled -> if (isRomaji) MODE_LABEL_JAPANESE else MODE_LABEL_ENGLISH
        else -> MODE_LABEL_JAPANESE_ONLY
    }

    private fun spaceFlickMap(options: RomajiFlick12Options): Map<FlickDirection, FlickAction> = buildMap {
        put(FlickDirection.TAP, FlickAction.Action(KeyAction.Space))
        mapOf(
            FlickDirection.UP to options.spaceUp,
            FlickDirection.UP_LEFT_FAR to options.spaceLeft,
            FlickDirection.DOWN to options.spaceDown,
            FlickDirection.UP_RIGHT_FAR to options.spaceRight,
        ).forEach { (direction, assignment) ->
            when (assignment) {
                RomajiFlick12SpaceFlick.HALF_WIDTH_SPACE ->
                    put(direction, FlickAction.Action(KeyAction.ForceHalfWidthSpace, label = "半角"))
                RomajiFlick12SpaceFlick.FULL_WIDTH_SPACE ->
                    put(direction, FlickAction.Action(KeyAction.ForceFullWidthSpace, label = "全角"))
                RomajiFlick12SpaceFlick.KATAKANA ->
                    put(direction, FlickAction.Action(KeyAction.ToggleKatakana, label = "カナ"))
                RomajiFlick12SpaceFlick.NONE -> Unit
            }
        }
    }

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

    private val englishPunctuation = mapOf("。" to ".", "、" to ",")

    private fun FlickKeySpec.toFlickMap(isRomaji: Boolean): Map<FlickDirection, FlickAction> {
        fun localized(text: String) = if (isRomaji) text else englishPunctuation[text] ?: text
        return buildMap {
            put(FlickDirection.TAP, input(center))
            up?.let { put(FlickDirection.UP, input(localized(it))) }
            left?.let { put(FlickDirection.UP_LEFT_FAR, input(localized(it))) }
            right?.let { put(FlickDirection.UP_RIGHT_FAR, input(localized(it))) }
            down?.let { put(FlickDirection.DOWN, input(localized(it))) }
        }
    }

    private fun input(text: String): FlickAction =
        FlickAction.Input(text, label = text.uppercase())
}
