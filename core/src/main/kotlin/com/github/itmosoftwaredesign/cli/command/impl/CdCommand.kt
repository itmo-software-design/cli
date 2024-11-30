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
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


/**
 *  Change working directory command.
 *
 *  Can producer the next errors:
 *  1. No argument passed
 *  2. Passed path is not found
 *  3. Passed path is not a directory
 *
 * @author mk17ru
 * @since 0.0.1
 */
class CdCommand : Command {
    private val HOME_DIR = System.getProperty("user.home");

    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ): CommandResult {
        if (arguments.size > 1) {
            errorStream.writeLineUTF8("Change directory command except <= 1 argument")
            return ErrorResult(1)
        }
        val move = if (arguments.isEmpty()) HOME_DIR else arguments[0]
        val newWorkingDirectory = environment.workingDirectory
            .resolve(move)
            .normalize()
            .toAbsolutePath()
        if (!newWorkingDirectory.exists()) {
            errorStream.writeLineUTF8("Directory '$newWorkingDirectory' does not exist")
            return ErrorResult(2)
        }
        if (!newWorkingDirectory.isDirectory()) {
            errorStream.writeLineUTF8("'$newWorkingDirectory' is not directory")
            return ErrorResult(3)
        }
        environment.workingDirectory = newWorkingDirectory
        return SuccessResult()
    }
}
