package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandResult
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.command.SuccessResult
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * `LsCommand` - команда для отображения содержимого каталога.
 *
 * Если аргументы отсутствуют, выводится содержимое текущего рабочего каталога.
 * Если указан каталог в аргументах, выводится его содержимое.
 *
 * @author mk17ru
 * @since 0.0.3
 */
class LsCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ): CommandResult {
        val move = if (arguments.isEmpty()) "." else arguments[0]
        val directory = environment.workingDirectory
            .resolve(move)
            .normalize()
            .toAbsolutePath()
        if (!directory.exists()) {
            errorStream.writeLineUTF8("Directory '$directory' does not exist")
            return ErrorResult(2)
        }
        if (!directory.isDirectory()) {
            outputStream.writeLineUTF8(directory.name)
            return SuccessResult()
        }
        try {
            val files = directory.listDirectoryEntries()
            files.sortedBy { it.name }.forEach { file ->
                outputStream.writeLineUTF8(file.name)
            }
        } catch (e : Exception) {
            errorStream.writeLineUTF8("ls: cannot read directory '${directory.toAbsolutePath()}'")
            return ErrorResult(4)
        }
        return SuccessResult()
    }
}
