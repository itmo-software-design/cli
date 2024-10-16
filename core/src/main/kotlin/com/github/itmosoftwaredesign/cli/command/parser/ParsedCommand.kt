package com.github.itmosoftwaredesign.cli.command.parser

import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 *  Parsed command description
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class ParsedCommand(
    val commandTokens: List<String>,
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val errorStream: OutputStream,
    val outputStreamFile: File = File(""),
    val inputStreamFile: File = File(""),
    val errorStreamFile: File = File("")
) : AutoCloseable {

    /**
     * Close not system stream on command close
     */
    override fun close() {
        var exception: Exception? = null
        try {
            if (inputStream !== System.`in`) {
                inputStream.close()
            }
        } catch (e: Exception) {
            exception = e
        }
        try {
            if (outputStream !== System.out) {
                outputStream.close()
            }
        } catch (e: Exception) {
            if (exception == null) {
                exception = e
            } else {
                exception.addSuppressed(e)
            }
        }
        try {
            if (errorStream !== System.err) {
                errorStream.close()
            }
        } catch (e: Exception) {
            if (exception == null) {
                exception = e
            } else {
                exception.addSuppressed(e)
            }
        }
        if (exception != null) {
            throw exception
        }
    }
}