package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandInterrupted
import com.github.itmosoftwaredesign.cli.command.CommandResult
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Exit` command definition.
 *
 *  Exits the CLI with a status of N.
 *  If N is omitted, the exit status is that of the last command executed.
 *
 *  If passed more than one argument, it will return an error `1`.
 *
 * @author sibmaks
 * @since 0.0.3
 */
class ExitCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ): CommandResult {
        if (arguments.isEmpty()) {
            return CommandInterrupted(environment.lastStatusCode)
        }
        if (arguments.size != 1) {
            errorStream.writeLineUTF8("exit command except expect 1 argument")
            return ErrorResult(1)
        }
        val statusCode = arguments.first().toInt()
        return CommandInterrupted(statusCode)
    }
}
