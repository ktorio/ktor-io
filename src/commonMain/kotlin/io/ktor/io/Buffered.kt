package io.ktor.io

abstract class BufferedBytesSource(val bufferSize: Int) : BytesSource()
abstract class BufferedBytesDestination(val bufferSize: Int) : BytesDestination()