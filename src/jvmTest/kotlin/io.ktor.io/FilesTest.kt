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

        val originalFile = Files.createFile(Path.of("original"))
        originalFile.writeText(content)
        val fileCopy = Path.of("copy")

        val source = FileBytesSource(originalFile)
        val destination = FileBytesDestination(fileCopy)

        while (source.canRead()) {
            val buffer = source.read()
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

        val originalFile = Files.createFile(Path.of("original"))
        originalFile.writeText(content)
        val fileCopy = Path.of("copy")

        val source = BufferedBytesSource(FileBytesSource(originalFile))
        val destination = BufferedBytesDestination(FileBytesDestination(fileCopy), 12 * 1024)

        while (source.canRead()) {
            val buffer = source.read()
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
}