package io.ktor.io

import java.lang.Integer.min
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
    override var closeCause: Throwable? = null
        private set

    override fun canWrite(): Boolean {
        closeCause?.let { throw it }
        return true
    }

    override fun write(buffer: Buffer) {
        try {
            if (buffer is ByteBufferJvm) {
                channel.write(buffer.buffer)
                return
            }
            DefaultByteBufferPool.useInstance { byteBuffer ->
                val toWrite = min(buffer.readCapacity(), byteBuffer.remaining())
                repeat(toWrite) {
                    byteBuffer.put(buffer.readByte())
                }
                byteBuffer.flip()
                channel.write(byteBuffer)
            }
        } catch (cause: Throwable) {
            close(cause)
            throw cause
        }
    }

    public override suspend fun awaitFreeSpace() {}

    override suspend fun flush() {}

    override fun close(cause: Throwable?) {
        closeCause = cause
        channel.close()
    }

    override fun close() {
        close(null)
    }
}
