# Source & Destination

## Source

There are lots of different places we need to read from, so we need to have some abstraction about these places. Here
the `Source` comes.

### Reading Bytes

The most basic operation is `read`: it gives you a new `Buffer` with data from the source:

```kotlin
val source = mySocket.input
val buffer = source.read()
```

The `Buffer` will have the data stored between `readPosition` and `writePosition`.

It's safe to call `read` multiple times without consuming previous result. It must always return a new `Buffer`:

```kotlin
val first = source.read()
val second = source.read()

assertTrue(first !== second)
```

If there are no bytes available at the moment, the `read` operation will return empty `Buffer`:

```kotlin
fun Source.readAvailable(): List<Buffer> {
    val chunks = mutableListOf<Buffer>()
    while (true) {
        val buffer = source.read()
        if (buffer.isEmpty()) return

        chunks.add(buffer)
    }

    return chunks
}
```

### Waiting for Bytes

What should I do if I want to read a specific amount of bytes or the whole `Source` to the `ByteArray`? If there are no
bytes available you have to wait. Waiting could be expensive operation: it can require system calls or context switch.
Spinning on the `read()` call also seems not a good idea. For these reasons, there is a separate `awaitBytes()`
operation.

`awaitBytes` is a `suspend` operation: it will suspend the current coroutine until the source has some data available,
or it will be closed:

```kotlin
abstract class Source {
    ...
    suspend fun awaitBytes(): Boolean
}
```

`awaitBytes` returns `true` is there is some data available. It returns `false` if the source is closed.

```kotlin

import javax.print.attribute.standard.Destination

-Source
-cancel and cancelCause
-Destination
-write
-awaitFreeSpace
-close
```

### What should I do if I don't need `Source` any more?

Imagine that you have a method to read a message from `Source`:

```kotlin
fun Source.parseMessage(): Message = TODO()
```

Of course, it can fail:

```kotlin
val message = try {
    socketSource.parseMessage()
} catch (cause: ParseException) {
    log.error("Failed to parse message", cause)
    throw cause
}
```

The source doesn't know about your message format, protocols and other stuff and of course it will continue reading data
from socket. If you want to be a nice citizen, you should not waste time or memory on reading bytes and release all
resources. You can do this, by cancelling the `Source`:

```kotlin
val message = try {
    socketSource.parseMessage()
} catch (cause: ParseException) {
    log.error("Failed to parse message", cause)
} catch (cause: Throwable) {
    socketSource.cancel(cause)
}
```

This is a common pattern of using `Source`s. If you don't need logging here, you can use the `use` shortcut:

```kotlin
val message = socketSource.use {
    it.parseMessage()
}
```

The `Source` will store an exception in the `cancelCause` property.

```kotlin
abstract class Source {
    abstract val cancelCause: Throwable?

    abstract fun read(): Buffer

    abstract suspend fun awaitBytes(): Boolean

    abstract fun cancel(cause: Throwable = CancellationException("Source has been cancelled"))
}
```

## Destination

To handle different outputs there is another abstraction: `Destination`. It is responsible for abstracting different
outputs.

To write data to the destination we can use `write` method:

```kotlin
val destination = mySocket.destination

val count = destination.write(buffer)
println("$count bytes written")
```

Write will write as much data(between `readPosition` and `writePosition`) it can to the destination. It will
modify `readPosition` and return how many bytes were written.

While the `Source` is responsible for creating `Buffer`, `Destination` is responsible for releasing it: if the `buffer`
has no bytes available after write, it will be automatically released.

How could you write a whole `Buffer` to the IO? The `spin` on the `write` method is not a good idea, so there is a
method `awaitFreeSpace()` that will suspend until the destination has some free space to write:

```kotlin
fun Destination.writeBuffer(buffer: Buffer) {
    while (true) {
        write(buffer)

        if (buffer.isEmpty()) return
        awaitFreeSpace()
    }
}
```

### How to end the conversation?

With `close()` method It will terminate `Destination` and release all resources. If there are some pending bytes they
will be sent:

```kotlin
fun Destination.writeTrailer(trailer: ByteArray) {
    writeFully(trailer)
    close()
}
```

It also can be useful to save some additional information about why and where `Destination` was closed, so you can pass
`cause` to the close method. There is also `isClosed` flag to check if the destination is already closed.

### Flush

It's hard to write human-readable justification of having flush method without introducing a new abstractions, so I will
keep it as TODO("Rewrite this").

Not human-readable justification:

If you have chained transformations on the `Destination` like:

```kotlin
val destination = mySocket.destination
    .compressed()
    .buffered()
```

and a single `message`. There is a common pattern when you want to send this message and make sure that all data is
actually written to file or socket(for instance it can be a http message header). Some transformations could keep an
internal buffer because of efficiency reasons.

To make sure that all data is written to the destination, you can call `flush()`. Flush is a suspend operation which
will suspend until all data is actually written to the final destination:

```kotlin
destination.writeFully(message)
destination.flush() 
// At this point message should be compressed and written to the socket.
```

## Final Listing

```kotlin
abstract class Destination {
    abstract val isClosed: Boolean
    abstract val closeCause: Throwable?

    abstract fun write(buffer: Buffer): Int

    abstract suspend fun awaitFreeSpace()

    abstract suspend fun flush()

    abstract fun close(cause: Throwable? = null)
}
```
