package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

class CdCommandTest {

    private lateinit var environment: Environment
    private lateinit var changeDirectoryCommand: CdCommand
    private lateinit var outputStream: ByteArrayOutputStream
    private lateinit var errorStream: ByteArrayOutputStream
    private val HOME_DIR = System.getProperty("user.home");


    @BeforeEach
    fun setUp() {
        environment = mockk(relaxed = true)
        changeDirectoryCommand = CdCommand()
        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()
    }

    @Test
    fun `should change working directory when correct argument provided`() {
        val currentDir = Files.createTempDirectory("current")
        currentDir.resolve("child").createDirectory()
        val newDir = "child"
        val expectedDir = currentDir.resolve(newDir).normalize().toAbsolutePath()

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(newDir))

        verify { environment.workingDirectory = expectedDir }
    }

    @Test
    fun `should not change working directory when not existed directory provided`() {
        val currentDir = Path.of(UUID.randomUUID().toString())
        val newDir = "child"

        every { environment.workingDirectory } returns currentDir

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(newDir))

        verify (exactly = 0) { environment.workingDirectory = any() }
    }

    @Test
    fun `should not change working directory when not directory provided`() {
        val currentDir = Files.createTempDirectory("current")
        currentDir.resolve("child").createFile()
        val newDir = "child"

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(newDir))

        verify (exactly = 0) { environment.workingDirectory = any() }
    }

    @Test
    fun `should change upper  directory when not directory provided`() {
        val currentDir = Files.createTempDirectory("current")

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf())

        verify  { environment.workingDirectory = Path.of(HOME_DIR) }
    }

    @Test
    fun `should change upper two dirs`() {
        val currentDir = Files.createTempDirectory("current")

        val parentSecond = currentDir.parent.parent

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf("../.."))

        verify  { environment.workingDirectory = parentSecond }
    }

    @Test
    fun `should change upper  directory when two points provided`() {
        val currentDir = Files.createTempDirectory("current")

        every { environment.workingDirectory } returns currentDir.toAbsolutePath()

        val parent = currentDir.parent

        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf(".."))

        verify  { environment.workingDirectory = parent }
    }

    @Test
    fun `should write error when too many arguments provided`() {
        changeDirectoryCommand.execute(environment, System.`in`, outputStream, errorStream, listOf("dir1", "dir2"))

        val errorMessage = "Change directory command except <= 1 argument"
        assertEquals(errorMessage, errorStream.toString().trim())
    }


}