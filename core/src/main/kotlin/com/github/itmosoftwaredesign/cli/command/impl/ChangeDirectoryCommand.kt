package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.writeUTF8
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.io.OutputStream

/**
 *  Change working directory command.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class ChangeDirectoryCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ) {
        if (arguments.size != 1) {
            errorStream.writeUTF8("Change directory command except expect 1 argument")
            return
        }
        val move = arguments[0]
        environment.workingDirectory = environment.workingDirectory
            .resolve(move)
            .normalize()
            .toAbsolutePath()
    }
}
