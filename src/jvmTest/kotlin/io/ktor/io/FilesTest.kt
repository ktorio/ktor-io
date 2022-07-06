package io.ktor.io

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class FilesTest {

    @Test
    fun testCopyingFile(): Unit = runBlocking {
        val content = buildString { repeat(100000) { append("testString$it") } }

        val originalFile = Files.createTempFile("test", "origin")
        originalFile.writeText(content)
        val fileCopy = Files.createTempFile("test", "copy")

        val source = FileBytesSource(originalFile)
        val destination = FileBytesDestination(fileCopy)

        while (source.canRead()) {
            val buffer = source.receive()
            destination.write(buffer)
            destination.awaitFreeSpace()
            source.awaitContent()
        }
        destination.flush()
        source.cancel()
        destination.close()

        val copiedContent = fileCopy.readText()

        originalFile.deleteIfExists()
        fileCopy.deleteIfExists()

        assertEquals(content, copiedContent)
    }

    @Test
    fun testCopyingFileBuffered(): Unit = runBlocking {
        val content = buildString { repeat(100000) { append("testString$it") } }

        val originalFile = Files.createTempFile("tmp", "original")
        originalFile.writeText(content)
        val fileCopy = Files.createTempFile("tmp", "copy")

        val source = Source(FileBytesSource(originalFile))
        val destination = BufferedBytesDestination(FileBytesDestination(fileCopy), 12 * 1024)

        var writeCount = 0
        while (source.canRead()) {
            val buffer = source.receive()
            val count = destination.write(buffer)
            writeCount += count

            println("Written $buffer $count/$writeCount/${content.length} bytes")

            destination.awaitFreeSpace()
            source.awaitContent()
        }

        destination.flush()
        source.cancel()
        destination.close()

        val copiedContent = fileCopy.readText()

        originalFile.deleteIfExists()
        fileCopy.deleteIfExists()

        assertEquals(content, copiedContent)
    }
}