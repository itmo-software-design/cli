package com.github.itmosoftwaredesign.cli.command

import com.github.itmosoftwaredesign.cli.Environment
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.io.OutputStream

/**
 * Command line interface command interface. Describe high level command functionality.
 *
 * @author sibmaks
 * @since 0.0.1
 */
interface Command {
    /**
     * Execute command
     *
     * @param environment  CLI environment
     * @param inputStream  command input stream
     * @param outputStream command output stream
     * @param errorStream  command output error stream
     * @param arguments    command arguments
     */
    fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    )
}
