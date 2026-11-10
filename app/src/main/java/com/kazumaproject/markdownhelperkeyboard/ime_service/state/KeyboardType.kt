package com.kazumaproject.markdownhelperkeyboard.ime_service.state

enum class KeyboardType {
    TENKEY, GOJUON, SUMIRE, QWERTY, ROMAJI, CUSTOM, SPLIT, MORSE, ROMAJI_FLICK12
}

val KeyboardType.isTenKeyFamily: Boolean
    get() = when (this) {
        KeyboardType.MORSE,
        KeyboardType.TENKEY,
        KeyboardType.GOJUON,
        KeyboardType.SUMIRE,
        KeyboardType.ROMAJI_FLICK12,
        KeyboardType.CUSTOM -> true

        KeyboardType.QWERTY,
        KeyboardType.ROMAJI,
        KeyboardType.SPLIT -> false
    }
