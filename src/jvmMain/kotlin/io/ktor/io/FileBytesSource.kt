package io.ktor.io

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ExecutorService
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
    override var closedCause: Throwable? = null
        private set

    private var state: JvmBuffer? = null

    override fun canRead(): Boolean {
        closedCause?.let { throw it }

        return !isClosedForRead
    }

    override fun read(): Buffer {
        closedCause?.let { throw it }

        return state.also { state = null } ?: Buffer.Empty
    }

    override suspend fun awaitContent() {
        closedCause?.let { throw it }

        val buffer = JvmBufferPool.Default.borrow()
        buffer.writeIndex = buffer.capacity

        val count = channel.read(buffer.buffer)
        if (count == -1) {
            isClosedForRead = true
            JvmBufferPool.Default.recycle(buffer)
            return
        }

        buffer.buffer.flip()
        bytesRead += count
        state = buffer
    }

    override fun cancel(cause: Throwable) {
        if (closedCause != null) return
        closedCause = cause
        channel.close()
    }

    private lateinit var readCompletionContinuation: Continuation<Int>

    private val readCompletionHandler = object : CompletionHandler<Int, Unit> {
        override fun completed(result: Int, attachment: Unit) {
            readCompletionContinuation.resume(result)
        }

        override fun failed(cause: Throwable, attachment: Unit) {
            if (cause is AsynchronousCloseException) {
                check(closedCause != null)
                readCompletionContinuation.resumeWithException(closedCause!!)
                return
            }

            cancel(cause)
            readCompletionContinuation.resumeWithException(cause)
        }
    }

    private suspend fun AsynchronousFileChannel.read(
        byteBuffer: ByteBuffer
    ): Int = suspendCoroutine { continuation ->
        readCompletionContinuation = continuation
        read(byteBuffer, bytesRead, Unit, readCompletionHandler)
    }
}
