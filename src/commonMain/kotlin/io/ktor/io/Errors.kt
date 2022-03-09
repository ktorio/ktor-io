package io.ktor.io

public expect open class IOException(message: String, cause: Throwable?) : Exception {
    public constructor(message: String)
}

public expect open class EOFException(message: String) : IOException
