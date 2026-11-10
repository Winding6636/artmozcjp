package com.kazumaproject.markdownhelperkeyboard.ime_service.romaji_kana

import org.junit.Assert.assertEquals
import org.junit.Test

class RomajiKanaConverterCustomLayoutTest {

    private val converter = RomajiKanaConverter(
        mapOf(
            "re" to Pair("れ", 2),
            "zo" to Pair("ぞ", 2),
            "ku" to Pair("く", 2),
            "na" to Pair("な", 2),
            "nn" to Pair("ん", 2),
            "ka" to Pair("か", 2),
        )
    )

    @Test
    fun singleNFollowedByAConsonantBecomesHatsuon() {
        assertEquals("れんz", converter.convertCustomLayout("renz"))
        assertEquals("れんぞく", converter.convertCustomLayout("renzoku"))
    }

    @Test
    fun nFollowedByAVowelStillMakesAnNaRowKana() {
        assertEquals("かな", converter.convertCustomLayout("kana"))
    }

    @Test
    fun trailingNStaysUntilTheNextKeyArrives() {
        assertEquals("れn", converter.convertCustomLayout("ren"))
    }

    @Test
    fun doubleNStillProducesOneHatsuon() {
        assertEquals("れん", converter.convertCustomLayout("renn"))
    }
}
