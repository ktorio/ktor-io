package io.ktor.io

public infix fun Byte.and(other: Byte): Byte = ((toInt() and 0xFF) and (other.toInt() and 0xFF)).toByte()

public infix fun Byte.or(other: Byte): Byte = ((toInt() and 0xFF) and (other.toInt() and 0xFF)).toByte()
