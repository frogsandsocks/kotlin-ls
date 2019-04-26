package tests

import ls.OutputFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path


internal class OutputFileTest {

    @Test
    @DisplayName("should initializes and closes")
    fun initAndClose (@TempDir tempDir: Path) {

        // Create temporary file
        val file = File(tempDir.toAbsolutePath().toString() + "/test.out")
        file.createNewFile()

        // Creating instance
        val output = OutputFile(file.absolutePath)

        // Closing instance
        output.close()
    }


    @Test
    @DisplayName("should successfully print with newline character")
    fun printWithNewline (@TempDir tempDir: Path) {

        // Create temporary file
        val file = File(tempDir.toAbsolutePath().toString() + "/test.out")
        file.createNewFile()

        // Creating instance
        val output = OutputFile(file.absolutePath)

        // Write some magic
        output.println("DEADBEEF")
        output.println("COFFEEED")

        // Closing instance
        output.close()

        // Checking file content
        val outputResult = String(file.readBytes())
        assertEquals("DEADBEEF\nCOFFEEED\n", outputResult)
    }


    @Test
    @DisplayName("should successfully print without newline character")
    fun printWithoutNewline (@TempDir tempDir: Path) {

        // Create temporary file
        val file = File(tempDir.toAbsolutePath().toString() + "/test.out")
        file.createNewFile()

        // Creating instance
        val output = OutputFile(file.absolutePath)

        // Write some magic
        output.print("CATSCATS")
        output.print("MEOWMEOW")

        // Closing instance
        output.close()

        // Checking file content
        val outputResult = String(file.readBytes())
        assertEquals("CATSCATSMEOWMEOW", outputResult)
    }
}
