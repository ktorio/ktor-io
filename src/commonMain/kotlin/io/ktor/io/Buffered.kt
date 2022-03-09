package io.ktor.io

public abstract class BufferedBytesSource(private val bufferSize: Int) : BytesSource()
public abstract class BufferedBytesDestination(private val bufferSize: Int) : BytesDestination()