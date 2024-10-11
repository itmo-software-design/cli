package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path

class PrintWorkingDirectoryCommandTest {

    private lateinit var printWorkingDirectoryCommand: PrintWorkingDirectoryCommand
    private lateinit var environment: Environment
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream
    private lateinit var inputStream: InputStream

    @BeforeEach
    fun setUp() {
        printWorkingDirectoryCommand = PrintWorkingDirectoryCommand()
        environment = mockk(relaxed = true)
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
        inputStream = mockk(relaxed = true)
    }

    @Test
    fun `should write current working directory to output stream`() {
        val workingDir = Path.of("/test/directory")
        every { environment.workingDirectory } returns workingDir

        printWorkingDirectoryCommand.execute(environment, inputStream, outputStream, errorStream, emptyList())

        val result = outputStream.toString().trim()
        assertEquals(workingDir.toAbsolutePath().toString(), result)
    }
}