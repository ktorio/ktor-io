package io.ktor.io.utils

public inline val Short.highByte: Byte get() = (toInt() ushr 8).toByte()
public inline val Short.lowByte: Byte get() = (toInt() and 0xff).toByte()
public inline val Int.highShort: Short get() = (this ushr 16).toShort()
public inline val Int.lowShort: Short get() = (this and 0xffff).toShort()
public inline val Long.highInt: Int get() = (this ushr 32).toInt()
public inline val Long.lowInt: Int get() = (this and 0xffffffffL).toInt()

internal inline fun Byte.asHighByte(lowByte: Byte): Short = ((toInt() shl 8) or (lowByte.toInt() and 0xff)).toShort()
internal inline fun Short.asHughShort(lowShort: Short): Int = (toInt() shl 16) or (lowShort.toInt() and 0xffff)
internal inline fun Int.asHighInt(lowInt: Int): Long = (toLong() shl 32) or (lowInt.toLong() and 0xffffffffL)