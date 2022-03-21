package io.ktor.io

import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public fun FileBytesSource(
    file: Path,
    options: Set<OpenOption> = setOf(StandardOpenOption.READ),
    executor: ExecutorService? = null
): FileBytesSource = FileBytesSource(AsynchronousFileChannel.open(file, options, executor))

public class FileBytesSource(private val channel: AsynchronousFileChannel) : BytesSource() {

    @Volatile
    private var bytesRead = 0L

    @Volatile
    private var isClosedForRead = false

    @Volatile
    override var closeCause: Throwable? = null
        private set

    private var buffer: Buffer? = null

    override fun canRead(): Boolean {
        closeCause?.let { throw it }

        return !isClosedForRead
    }

    override fun read(): Buffer {
        return buffer.also { buffer = null } ?: EmptyBuffer
    }

    override suspend fun awaitContent() {
        val buffer = ByteBufferJvmPool.borrow()
        val byteBuffer = buffer.buffer
        val read = suspendCancellableCoroutine<Int> { continuation ->
            channel.read(byteBuffer, bytesRead, Unit, object : CompletionHandler<Int, Unit> {
                override fun completed(result: Int, attachment: Unit) {
                    continuation.resume(result)
                }

                override fun failed(cause: Throwable, attachment: Unit) {
                    if (cause is AsynchronousCloseException) {
                        continuation.resumeWithException(closeCause!!)
                        return
                    }

                    cancel(cause)
                    continuation.resumeWithException(cause)
                }
            })
        }
        bytesRead += read
        isClosedForRead = read == -1
        byteBuffer.flip()
        this.buffer = buffer
    }

    override fun cancel(cause: Throwable) {
        closeCause = cause
        channel.close()
    }
}
