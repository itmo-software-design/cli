package com.github.itmosoftwaredesign.cli

import java.io.InputStream

class ObservableInputStream(
    private val inputStream: InputStream,
    val onRead: () -> Unit
) : InputStream() {

    override fun available(): Int {
        return inputStream.available()
    }

    override fun read(): Int {
        onRead()
        return inputStream.read()
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        onRead()
        return inputStream.read(b, off, len)
    }

    override fun read(b: ByteArray): Int {
        onRead()
        return inputStream.read(b)
    }

    override fun readAllBytes(): ByteArray {
        onRead()
        return inputStream.readAllBytes()
    }

    override fun readNBytes(len: Int): ByteArray {
        onRead()
        return inputStream.readNBytes(len)
    }

    override fun readNBytes(b: ByteArray?, off: Int, len: Int): Int {
        onRead()
        return inputStream.readNBytes(b, off, len)
    }

}