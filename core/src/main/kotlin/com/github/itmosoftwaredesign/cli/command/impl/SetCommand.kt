package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandResult
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.command.SuccessResult
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Set` command definition.
 *
 *  The `set` command change variable value in environment.
 *
 *  It can create a new value or rewrite existing one.
 *
 * @author sibmaks
 * @since 0.0.3
 */
class SetCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ): CommandResult {
        if (arguments.isEmpty()) {
            errorStream.writeLineUTF8("At least one argument excepted")
            return ErrorResult(1)
        }
        val variableName = arguments[0]
        val variableValue = if(arguments.size == 1) null else arguments.subList(1, arguments.size).joinToString(" ")
        environment.setVariable(variableName, variableValue)
        return SuccessResult()
    }
}
