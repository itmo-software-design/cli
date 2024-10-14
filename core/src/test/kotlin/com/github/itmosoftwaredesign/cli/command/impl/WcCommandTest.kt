package com.github.itmosoftwaredesign.cli.command.impl

import EchoCommand
import WcCommand
import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class WcCommandTest {

    private lateinit var wcCommand: WcCommand
    private lateinit var environment: Environment
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream
    private lateinit var inputStream: ByteArrayInputStream

    @BeforeEach
    fun setUp() {
        wcCommand = WcCommand()
        environment = mockk(relaxed = true)
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should read file from input stream and write to output stream`() {
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("1\n2\t3\n456\n\n")

        wcCommand.execute(
            environment, ByteArrayInputStream(byteArrayOf()), outputStream, errorStream, listOf(tempFile.absolutePath)
        )

        val expected = "    4    4    11 ${tempFile.absolutePath}\n"
        assertEquals(expected, outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should throw file not found exception`() {
        val nonExistentFile = "nonExistentFile.txt"

        wcCommand.execute(
            environment, ByteArrayInputStream(byteArrayOf()), outputStream, errorStream, listOf(nonExistentFile)
        )

        assertTrue(errorStream.toString().contains("Reading input file $nonExistentFile exception, reason: $nonExistentFile (No such file or directory)"))
    }
}