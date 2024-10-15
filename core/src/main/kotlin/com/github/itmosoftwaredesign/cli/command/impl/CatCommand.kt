package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Cat` command definition
 *
 * @author sibmaks
 * @since 0.0.1
 */
class CatCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ) {
        if (arguments.isEmpty()) {
            var c: Int
            while ((inputStream.read().also { c = it }) != -1) {
                outputStream.write(c)
            }
            return
        }
        for (argument in arguments) {
            try {
                FileInputStream(argument).use { fileInputStream ->
                    fileInputStream.transferTo(outputStream)
                }
            } catch (e: IOException) {
                errorStream.writeLineUTF8("File '$argument' read exception, reason: ${e.message}")
            }
        }
    }
}
