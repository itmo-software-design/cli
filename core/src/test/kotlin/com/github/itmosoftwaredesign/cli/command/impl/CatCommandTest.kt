package com.github.itmosoftwaredesign.cli.command.impl

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

class CatCommandTest {

    private lateinit var catCommand: CatCommand
    private lateinit var environment: Environment
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream
    private lateinit var inputStream: ByteArrayInputStream

    @BeforeEach
    fun setUp() {
        catCommand = CatCommand()
        environment = mockk(relaxed = true)
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should read from input stream and write to output stream when no arguments provided`() {
        val inputText = UUID.randomUUID().toString()
        inputStream = ByteArrayInputStream(inputText.toByteArray())

        // Simulating input ending with Ctrl-D (EOF)
        catCommand.execute(
            environment, inputStream, outputStream, errorStream, emptyList()
        )

        assertEquals(inputText, outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())
    }

    @Test
    fun `should read from file and write to output stream when file argument is provided`() {
        val tempFile = File.createTempFile("test", ".txt")
        val content = UUID.randomUUID().toString()
        tempFile.writeText(content)

        val rs = catCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(tempFile.absolutePath)
        )

        assertEquals(0, rs.exitCode)
        assertEquals(content, outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should write error message when file does not exist`() {
        val nonExistentFile = "nonexistentfile.txt"

        catCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(nonExistentFile)
        )

        assertTrue(errorStream.toString().contains("File '$nonExistentFile' read exception"))
    }

    @Test
    fun `should handle multiple files and output their contents`() {
        val tempFile1 = File.createTempFile("test1", ".txt")
        val content1 = UUID.randomUUID().toString()
        tempFile1.writeText(content1)
        val tempFile2 = File.createTempFile("test2", ".txt")
        val content2 = UUID.randomUUID().toString()
        tempFile2.writeText(content2)

        catCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(tempFile1.absolutePath, tempFile2.absolutePath)
        )

        assertEquals("$content1$content2", outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile1.deleteOnExit()
        tempFile2.deleteOnExit()
    }

    @Test
    fun `should handle mixed existing and non-existing files`() {
        val tempFile = File.createTempFile("test", ".txt")
        val content = UUID.randomUUID().toString()
        tempFile.writeText(content)
        val nonExistentFile = "nonexistentfile.txt"

        catCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(tempFile.absolutePath, nonExistentFile)
        )

        assertEquals(content, outputStream.toString())
        assertTrue(errorStream.toString().contains("File '$nonExistentFile' read exception"))

        tempFile.deleteOnExit()
    }
}