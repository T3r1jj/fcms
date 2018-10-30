package io.github.t3r1jj.fcms.external

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MegaTest {
    @Test
    fun testReplace() {
        val string = java.lang.String("d---    -          - 29Oct2018 18:24:52 io.github.t3r1jj.fcms.external")
        val string2 = "d---    -          - 29Oct2018 18:24:52 io.github.t3r1jj.fcms.external"
        val words = string.split("\\s+")
        val words2 = string2.split("\\s+")
        assertEquals(6, words.size)
        assertNotEquals(words.size, words2.size)
    }
}