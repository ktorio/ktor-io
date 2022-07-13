package io.ktor.io

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class EmptyBufferTest {
    @Test
    fun testChangeIndexes() {
        assertFailsWith<IllegalArgumentException> { Buffer.Empty.writeIndex++ }
        assertFailsWith<IllegalArgumentException> { Buffer.Empty.readIndex++ }

        Buffer.Empty.writeIndex = 0
        Buffer.Empty.readIndex = 0
    }

    @Test
    fun testStealDoesNothing() {
        assertSame(Buffer.Empty, Buffer.Empty.steal())
    }

    @Test
    fun testGetFails() {
        assertFailsWith<IndexOutOfBoundsException> { Buffer.Empty[0] }
        assertFailsWith<IndexOutOfBoundsException> { Buffer.Empty[0] = 42 }
    }
}