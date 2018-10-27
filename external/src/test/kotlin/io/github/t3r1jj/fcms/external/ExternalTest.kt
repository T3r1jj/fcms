package io.github.t3r1jj.fcms.external

import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalTest {
    private val external = External()

    @Test
    fun testExternal() {
        assertEquals(5, external.sum(2, 3))
    }
}