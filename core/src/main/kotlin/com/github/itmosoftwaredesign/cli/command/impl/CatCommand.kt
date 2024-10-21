package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandResult
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.command.SuccessResult
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Cat` command definition.
 *
 *  The `cat` command displays file contents.
 *
 *  It reads one or multiple files and prints their content to the terminal.
 *
 *  `cat` is used to view file contents, combine files, and create new files.
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
    ): CommandResult {
        if (arguments.isEmpty()) {
            var c: Int
            while ((inputStream.read().also { c = it }) != -1) {
                outputStream.write(c)
            }
            return SuccessResult()
        }
        var hasException = false
        for (argument in arguments) {
            try {
                FileInputStream(argument).use { fileInputStream ->
                    fileInputStream.transferTo(outputStream)
                }
            } catch (e: IOException) {
                hasException = true
                errorStream.writeLineUTF8("File '$argument' read exception, reason: ${e.message}")
            }
        }
        if (hasException) {
            return ErrorResult(1)
        }
        return SuccessResult()
    }
}
