package com.github.itmosoftwaredesign.cli

import java.io.OutputStream

/**
 * Write string as UTF-8 to output stream.
 *
 * @param string string to write
 * @receiver output stream
 */
fun OutputStream.writeUTF8(string: String) {
    write(string.toByteArray(Charsets.UTF_8))
}

/**
 * Write string as UTF-8 to output stream with line separator at the end
 *
 * @param string string to write
 * @receiver output stream
 */
fun OutputStream.writeLineUTF8(string: String) {
    writeUTF8(string)
    writeUTF8(System.lineSeparator())
}