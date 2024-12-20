package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandResult
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.command.SuccessResult
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Echo` command definition.
 *
 *  The echo command prints out its arguments as standard output.
 *  It is used to display text strings or the command results.
 *
 * @author gkashin
 * @since 0.0.1
 */
class EchoCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ): CommandResult {
        val joined = arguments.joinToString(separator = " ")
        try {
            outputStream.writeLineUTF8(joined)
            return SuccessResult()
        } catch (e: IOException) {
            errorStream.writeLineUTF8("Output stream write exception, reason: ${e.message}")
            return ErrorResult(1)
        }
    }
}
