## Buffer

The goal of IO library is to provide a common interface for all the different types of inputs and outputs, considering
usability and performance.

The main purpose of IO is read and write arrays of bytes. There is a `ByteArray` - common Kotlin array of bytes
abstraction, but it usually is not the efficient way to represent array of bytes on the platform(`ByteBuffer` on JVM,
`NSData` on iOS, `CPointer` on Native, `ArrayBuffer` on Js and so on).

So firstly we need a primitive type to abstract array of bytes.

The `Buffer` is an abstraction to hold some platform primitive byte storage and give simple access to it.

`Buffer` can hold a `ByteArray`:

```kotlin
val message = Buffer("Hello, World!".encodeToByteArray())
```

You can also allocate buffer with `ByteArray` directly:

```kotlin
val message = ByteArrayBuffer(size = 1024)
```

And has most of the `ByteArray` methods:

```kotlin
println("My message size is: ${message.size}")
println("It starts with ${buffer[0]}")
```

`Buffer` is also mutable:

```kotlin
fun Buffer.fill(value: Byte) {
    for (position in buffer.indices) {
        buffer[position] = value
    }
}

message.fill(0.toByte())
```

## Where My Data is Located?

Imagine that you want to read some data from stdin. You don't know how much data to expect, so you will likely allocate
some buffer with big enough size in advance:

```kotlin
/** Reads StdIn to a [buffer] and returns read count */
fun readFromStdInTo(buffer: Buffer): Int = TODO()

val buffer = ByteArrayBuffer(size = 1024)
val readCount = readFromStdInTo(buffer)
```

Now you have a problem: you have to pass some position and `readCount` across all usages of this `buffer`.
Moreover, if you want to use this buffer again without consuming all data, you will need to make all API accepting
indexes:

```kotlin
/** Reads StdIn to a [buffer] and returns read count */
fun readFromStdInTo(buffer: Buffer, offset: Int): Int = TODO()

val MESSAGE_HEADER_SIZE = 10
val buffer = ByteArrayBuffer(size = 1024)

var writeIndex = 0
while (writeIndex < MESSAGE_HEADER_SIZE) {
    bytesAvailable += readFromStdInTo(buffer, writeIndex)
}
```

Already smells a bit?
Let's implement method that will read data from one buffer and write it to another:

```kotlin
fun copyAndLog(
    from: Buffer,
    fromOffset: Int,
    readLength: Int,
    to: Buffer,
    toWriteOffset: Int
): Int {
    for (index in 0 until readLength) {
        val fromIndex = fromOffset + index
        val toIndex = toWriteOffset + index

        val byte = from[fromIndex]
        println("Copy byte: $byte")

        to[toIndex] = byte
    }
}
```

Or you can write it like this:

```kotlin
fun copyAndLog(
    from: Buffer,
    fromStartIndex: Int,
    fromEndIndex: Int,
    to: Buffer,
    toStartIndex: Int
): Int {
    for (index in 0 until (fromEndIndex - fromStartIndex)) {
        val fromIndex = fromStartIndex + index
        val toIndex = toStartIndex + index

        val byte = from[fromIndex]
        println("Copy byte: $byte")
        to[toIndex] = byte
    }
}
```

So we have lots of opinionated ways of making a single stuff with indexes, offsets. It looks nasty in combination.

Let's make it a bit simpler:

## Buffer indexes

The `Buffer` keep track of positions where data is in the `readPosition`, and where you can write it in
the `writePosition`. They are public and mutable:

```kotlin
fun copyAndLog(from: Buffer, to: Buffer) {
    for (index in from.readPosition until from.writePosition) {
        val byte = from[index]
        println("Copy byte: $byte")
        to[index] = byte
    }

    val bytesCount = from.writePosition - from.readPosition
    to.writePosition += bytesCount
    from.readPosition += bytesCount
}
```

It looks similar to `ByteBuffer.position/limit`, but doesn't require developer to track the Buffer state.

## How to Connect the Buffer With All My Data Classes?

Imagine you have a class:

```kotlin
data class User(
    val id: Long,
    val name: String,
    val age: Int,
    val email: String
)
```

To send it over the network you need to convert it to bytes.
Using get and set methods are not convenient: you have to encode each field manually.

There are some utility methods to help you:

```kotlin
fun Buffer.writeUser(user: User) {
    with(user) {
        writeLong(id)
        writeInt(name.length)
        writeBytes(name.encodeToByteArray())
        writeInt(age)
        writeInt(email.length)
        writeBytes(email.encodeToByteArray())
    }
}
```

```kotlin
fun Buffer.readUser(): User {
    val id = readLong()
    val nameLength = readInt()
    val name = Strign(readBytes(nameLength))
    val age = readInt()
    val emailLength = readInt()
    val email = String(readBytes(emailLength))

    return User(id, name, age, email)
}
```
