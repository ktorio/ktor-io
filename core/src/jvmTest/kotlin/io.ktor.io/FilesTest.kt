package io.ktor.io

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
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

        while (source.awaitContent()) {
            val buffer = source.read()

            while (buffer.isNotEmpty) {
                destination.awaitFreeSpace()
                destination.write(buffer)
            }
        }

        destination.flush()
        destination.close()

        val copiedContent = fileCopy.readText()

        originalFile.deleteIfExists()
        fileCopy.deleteIfExists()

        assertEquals(content, copiedContent)
    }

    @Test
    fun testCopyingFileBuffered(): Unit = runBlocking {
        val content = buildString { repeat(100000) { append("testString$it") } }

        val originalFile = Files.createTempFile("file", "origin")
        originalFile.writeText(content)

        val fileCopy = Files.createTempFile("file", "copy")

        val source = BufferedSource(FileBytesSource(originalFile))
        val destination = BufferedDestination(FileBytesDestination(fileCopy), 12 * 1024)

        while (source.awaitContent()) {
            val buffer = source.read()

            while (buffer.isNotEmpty) {
                destination.awaitFreeSpace()
                destination.write(buffer)
            }
        }

        destination.flush()
        destination.close()

        val copiedContent = fileCopy.readText()

        originalFile.deleteIfExists()
        fileCopy.deleteIfExists()

        assertEquals(content, copiedContent)
    }
}