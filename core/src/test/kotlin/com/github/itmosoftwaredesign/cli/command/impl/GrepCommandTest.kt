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

class GrepCommandTest {

    private lateinit var grepCommand: GrepCommand
    private lateinit var environment: Environment
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream
    private lateinit var inputStream: ByteArrayInputStream

    @BeforeEach
    fun setUp() {
        grepCommand = GrepCommand()
        environment = mockk(relaxed = true)
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should find lines in the file according to the pattern`() {
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("" +
                "this is a test file to test grep command\n" +
                "THis is the second line ab\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "abc\n" +
                "ab\n"
        )

        val pattern = "this"
        val res = grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(pattern, tempFile.absolutePath)
        )

        assertEquals("this is a test file to test grep command\n", outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should find lines in the file according to the pattern with several options`() {
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("" +
                "this is a test file to test grep command\n" +
                "THis is the second line ab\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "abc\n" +
                "ab\n"
        )

        val pattern = "this"
        val res = grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(pattern, "-i", "-A", "1", tempFile.absolutePath)
        )

        assertEquals("this is a test file to test grep command\nTHis is the second line ab\nis\n", outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should find lines in the file according to the pattern with -i option`() {
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("" +
                "this is a test file to test grep command\n" +
                "THis is the second line ab\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "abc\n" +
                "ab\n"
        )

        val pattern = "this"
        val res = grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(pattern, "-i", tempFile.absolutePath)
        )

        assertEquals("this is a test file to test grep command\nTHis is the second line ab\n", outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should find lines in the file according to the pattern with -w option`() {
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("" +
                "this is a test file to test grep command\n" +
                "THis is the second line ab\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "abc\n" +
                "ab\n"
        )

        val pattern = "ab"
        val res = grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(pattern, "-w", tempFile.absolutePath)
        )

        assertEquals("THis is the second line ab\nab\n", outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should find lines in the file with -A option`() {
        val tempFile = File.createTempFile("test", ".txt")
        val content = "" +
                "this is a test file to test grep command\n" +
                "THis is the second line ab\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "abc\n" +
                "ab\n"
        tempFile.writeText(content)

        val pattern = "test"
        val res = grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(pattern, "-A", "1", tempFile.absolutePath)
        )

        assertEquals("this is a test file to test grep command\nTHis is the second line ab\n", outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should find lines in the file with -A option with lines intersection`() {
        val tempFile = File.createTempFile("test", ".txt")
        val content = "" +
                "this is a test file to test grep command\n" +
                "THis is the second line ab\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "is\n" +
                "abc\n" +
                "ab\n"
        tempFile.writeText(content)

        val pattern = "is"
        val res = grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(pattern, "-A", "2", tempFile.absolutePath)
        )

        assertEquals(content, outputStream.toString())
        assertTrue(errorStream.toString().isEmpty())

        tempFile.deleteOnExit()
    }

    @Test
    fun `should write error message when file doesn't exist`() {
        val nonExistentFile = "nonexistentfile.txt"

        grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf("arg1", nonExistentFile)
        )

        assertTrue(errorStream.toString().contains("File '$nonExistentFile' read exception"))
    }

    @Test
    fun `should write error message when wrong number of arguments provided`() {
        val nonExistentFile = "nonexistentfile.txt"

        grepCommand.execute(
            environment,
            ByteArrayInputStream(byteArrayOf()),
            outputStream,
            errorStream,
            listOf(nonExistentFile)
        )

        assertEquals(errorStream.toString(), "usage: grep [-abcdDEFGHhIiJLlMmnOopqRSsUVvwXxZz] [-A num] [-i string] [-w string] [file ...]\n")
    }
}