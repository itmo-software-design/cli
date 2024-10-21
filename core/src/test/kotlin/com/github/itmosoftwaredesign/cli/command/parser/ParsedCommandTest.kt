package com.github.itmosoftwaredesign.cli.command.parser

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.*

class parsedCommandsTest {

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var errorStream: OutputStream

    @BeforeEach
    fun setUp() {
        inputStream = mockk(relaxed = true)
        outputStream = mockk(relaxed = true)
        errorStream = mockk(relaxed = true)
    }

    @Test
    fun `should not close system streams when auto-closing`() {
        val parsedCommands = ParsedCommand(listOf("cmd"), System.`in`, System.out, System.err)

        try {
            parsedCommands.close()
        } catch (e: Exception) {
            fail(e)
        }

    }

    @Test
    fun `should close non-system streams without exceptions`() {
        val inputStream = ByteArrayInputStream("test input".toByteArray())
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        val parsedCommands = ParsedCommand(listOf("cmd"), inputStream, outputStream, errorStream)

        try {
            parsedCommands.close()
        } catch (e: Exception) {
            fail(e)
        }
    }

    @Test
    fun `should throw exception when stream close fails`() {
        val failingInputStream = mockk<InputStream> {
            every { close() } throws IOException("Failed to close input stream")
        }
        val parsedCommands = ParsedCommand(listOf("cmd"), failingInputStream, System.out, System.err)

        val exception = assertThrows<Exception> {
            parsedCommands.close()
        }

        assertTrue(exception.message?.contains("Failed to close input stream") ?: false)
    }

    @Test
    fun `should throw exception when multiple streams fail to close and suppress exceptions`() {
        val failingInputStream = mockk<InputStream> {
            every { close() } throws IOException("Failed to close input stream")
        }
        val failingOutputStream = mockk<OutputStream> {
            every { close() } throws IOException("Failed to close output stream")
        }

        val parsedCommands = ParsedCommand(listOf("cmd"), failingInputStream, failingOutputStream, System.err)

        val exception = assertThrows<Exception> {
            parsedCommands.close()
        }

        assertTrue(exception.message?.contains("Failed to close input stream") ?: false)
        assertTrue(exception.suppressed.any { it.message?.contains("Failed to close output stream") ?: false })
    }

    @Test
    fun `should add suppressed exception when error stream fails after output stream`() {
        val failingOutputStream = mockk<OutputStream> {
            every { close() } throws IOException("Failed to close output stream")
        }
        val failingErrorStream = mockk<OutputStream> {
            every { close() } throws IOException("Failed to close error stream")
        }

        val parsedCommands = ParsedCommand(listOf("cmd"), System.`in`, failingOutputStream, failingErrorStream)

        val exception = assertThrows<Exception> {
            parsedCommands.close()
        }

        assertTrue(exception.message?.contains("Failed to close output stream") ?: false)
        assertTrue(exception.suppressed.any { it.message?.contains("Failed to close error stream") ?: false })
    }
}