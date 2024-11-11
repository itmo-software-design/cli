package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

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

    @Test
    fun `should handle output stream IOException`() {
        val text = UUID.randomUUID().toString()
        val arguments = listOf(text)

        val outputStream = mockk<OutputStream>(relaxed = true)
        every { outputStream.write(text.toByteArray(Charsets.UTF_8)) } throws IOException("Test error")

        val result = echoCommand.execute(environment, System.`in`, outputStream, errorStream, arguments)

        assertEquals("Output stream write exception, reason: Test error\n", errorStream.toString())
        assert(result is ErrorResult)
        assertEquals(1, result.exitCode)
    }
}