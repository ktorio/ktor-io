package io.ktor.io

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
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
): FileSource = FileSource(AsynchronousFileChannel.open(file, options, executor))

public class FileSource(private val channel: AsynchronousFileChannel) : Source() {

    @Volatile
    private var bytesRead = 0L

    @Volatile
    private var isClosedForRead = false

    @Volatile
    override var cancelCause: Throwable? = null
        private set

    private var buffer: Buffer? = null

    override fun read(): Buffer {
        cancelCause?.let { throw it }

        return buffer.also { buffer = null } ?: Buffer.Empty
    }

    override suspend fun awaitContent(): Boolean {
        cancelCause?.let { throw it }

        val buffer = BufferPool.borrow()
        val byteBuffer = buffer.buffer
        val count = channel.read(byteBuffer)

        bytesRead += count
        isClosedForRead = count == -1
        byteBuffer.flip()
        this.buffer = buffer
        return count >= 0
    }

    override fun cancel(cause: Throwable) {
        if (cancelCause != null) return
        cancelCause = cause
        channel.close()
    }

    private lateinit var readCompletionContinuation: CancellableContinuation<Int>

    private val readCompletionHandler = object : CompletionHandler<Int, Unit> {
        override fun completed(result: Int, attachment: Unit) {
            readCompletionContinuation.resume(result)
        }

        override fun failed(cause: Throwable, attachment: Unit) {
            if (cause is AsynchronousCloseException) {
                check(cancelCause != null)
                readCompletionContinuation.resumeWithException(cancelCause!!)
                return
            }

            cancel(cause)
            readCompletionContinuation.resumeWithException(cause)
        }
    }

    private suspend fun AsynchronousFileChannel.read(
        byteBuffer: ByteBuffer
    ) = suspendCancellableCoroutine<Int> { continuation ->
        readCompletionContinuation = continuation
        read(byteBuffer, bytesRead, Unit, readCompletionHandler)
    }
}
