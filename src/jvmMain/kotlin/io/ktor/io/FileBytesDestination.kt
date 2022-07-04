package io.ktor.io

import java.lang.Integer.min
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute

public fun FileBytesDestination(
    file: Path,
    options: Set<OpenOption> = setOf(StandardOpenOption.CREATE, StandardOpenOption.WRITE),
    vararg attrs: FileAttribute<Any>
): FileBytesDestination = FileBytesDestination(FileChannel.open(file, options, *attrs))

public class FileBytesDestination(private val channel: FileChannel) : BytesDestination() {

    @Volatile
    override var closedCause: Throwable? = null
        private set

    override fun canWrite(): Boolean {
        if (closedCause is ClosedChannelException) return false
        closedCause?.let { throw it }
        return true
    }

    override fun write(buffer: Buffer) {
        closedCause?.let { throw it }

        try {
            if (buffer is DefaultBuffer) {
                channel.write(buffer.buffer)
                return
            }
            slowWrite(buffer)
        } catch (cause: Throwable) {
            close(cause)
            throw cause
        }
    }

    private fun slowWrite(buffer: Buffer) {
        DefaultDirectByteBufferPool.useInstance { byteBuffer ->
            val toWrite = min(buffer.readCapacity(), byteBuffer.remaining())
            if (buffer is ByteArrayBuffer) {
                byteBuffer.put(buffer.array, buffer.readIndex, toWrite)
                buffer.readIndex += toWrite
            } else {
                repeat(toWrite) {
                    byteBuffer.put(buffer.readByte())
                }
            }
            byteBuffer.flip()
            channel.write(byteBuffer)
        }
    }

    public override suspend fun awaitFreeSpace() {
        closedCause?.let { throw it }
    }

    override suspend fun flush() {
        closedCause?.let { throw it }
    }

    override fun close(cause: Throwable?) {
        closedCause = cause ?: ClosedChannelException()
        channel.close()
    }

    override fun close() {
        close(null)
    }
}
