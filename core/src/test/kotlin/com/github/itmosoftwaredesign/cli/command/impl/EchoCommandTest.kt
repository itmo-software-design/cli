package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EchoCommandTest {

    private lateinit var echoCommand: EchoCommand
    private lateinit var environment: Environment
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream
    private lateinit var inputStream: ByteArrayInputStream

    @BeforeEach
    fun setUp() {
        echoCommand = EchoCommand()
        environment = mockk(relaxed = true)
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should read from input stream and write to output stream`() {
        val expected = "123 abc, 456 def${System.lineSeparator()}"
        inputStream = ByteArrayInputStream("echo 123 abc, 456       def".toByteArray())
        val args = listOf("123 abc, 456 def")

        echoCommand.execute(
            environment, inputStream, outputStream, errorStream, args
        )

        assertEquals(expected, outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())
    }
}