package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.io.OutputStream

/**
 *  Print working directory command.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class PrintWorkingDirectoryCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ) {
        val workingDirectory = environment.workingDirectory
        outputStream.writeLineUTF8(workingDirectory.toAbsolutePath().toString())
    }
}
