package tests

import ls.main
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

@Nested
internal class CLITest {

    companion object {

        private var PLAYGROUND_DIRECTORY = ""
        private var OUTPUT_LOG = ""

        @BeforeAll
        @JvmStatic
        internal fun createPlayground(@TempDir tempDir : Path) {

            // Set the playground path
            PLAYGROUND_DIRECTORY = tempDir.toAbsolutePath().toString() + "/"
            OUTPUT_LOG = "$PLAYGROUND_DIRECTORY.output.log"

            // Creating output log file
            File(OUTPUT_LOG).createNewFile()

            // Create empty directory
            File(PLAYGROUND_DIRECTORY + "directory-empty").mkdir()


            // Create file for modify date test
            val metadataFile = File(PLAYGROUND_DIRECTORY + "single-file-check")
            metadataFile.createNewFile()

            // Fill size test file with random bytes
            val randomInstance = Random()
            val sizeTestContent = ByteArray(1024)
            randomInstance.nextBytes(sizeTestContent)
            metadataFile.writeBytes(sizeTestContent)
            metadataFile.setLastModified(0)


            // Create directory with files for listing test
            File(PLAYGROUND_DIRECTORY + "directory-files").mkdir()

            // Create four files with strange names for listing tests with more than one file
            val beautyFile = File(PLAYGROUND_DIRECTORY + "directory-files/beauty-file")
            val charmFile =  File(PLAYGROUND_DIRECTORY + "directory-files/charm-file")
            val strangeFile = File(PLAYGROUND_DIRECTORY + "directory-files/strange-file")
            val truthFile = File(PLAYGROUND_DIRECTORY + "directory-files/truth-file")

            beautyFile.createNewFile()
            charmFile.createNewFile()
            strangeFile.createNewFile()
            truthFile.createNewFile()


            // Set size for files
            val sizeFirstFile = ByteArray( 2 * 1024 )
            randomInstance.nextBytes(sizeFirstFile)
            beautyFile.writeBytes(sizeFirstFile)

            val sizeSecondFile = ByteArray( 3 * 1024 )
            randomInstance.nextBytes(sizeSecondFile)
            charmFile.writeBytes(sizeSecondFile)

            val sizeThirdFile = ByteArray( 4 * 1024 )
            randomInstance.nextBytes(sizeThirdFile)
            strangeFile.writeBytes(sizeThirdFile)

            val sizeFourthFile = ByteArray( 5 * 1024 )
            randomInstance.nextBytes(sizeFourthFile)
            truthFile.writeBytes(sizeFourthFile)


            // Set modification dates for files
            beautyFile.setLastModified(60)
            charmFile.setLastModified(120)
            strangeFile.setLastModified(180)
            truthFile.setLastModified(240)


            // Set permissions for files
            beautyFile.setReadable(false)
            strangeFile.setReadOnly()
            // Other directories are already readable and writable
        }
    }

    @Test
    @DisplayName("should correctly list short form of single inode (file / empty directory)")
    fun checkSingleInode() {

        // Run CLI command
        main( arrayOf("-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}directory-empty") )

        // Read log entry
        // val result = readAllLines(Paths.get(OUTPUT_LOG))
        val result = File(OUTPUT_LOG).readLines()[0]

        assertEquals("directory-empty", result, "Mismatch between expected and actual inode name")
    }


    @Test
    @DisplayName("should correctly list long form of the single file")
    fun checkFileLongForm() {

        // Create formatted output for January 1st, 1970 00:00:00
        val expectedDate = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(0))

        // Run CLI command
        main( arrayOf("-l", "-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}single-file-check") )

        // Parse output
        val result =
            File(OUTPUT_LOG).readLines()[0].split('\t')

        // Check permissions (default must be "rw-" or "110" in bit mask format) TODO
        assertEquals("110", result[0], "Mismatch between expected and actual permissions as bitmask")
        assertEquals("1024", result[1], "Mismatch between expected and actual file size in bytes")
        assertEquals(expectedDate, result[2], "Mismatch between expected and actual date")
        assertEquals("single-file-check", result[3], "Mismatch between expected and actual file name")
    }


    @Test
    @DisplayName("should correctly list long form in human-readable output of the single file")
    fun checkFileLongFormHumanReadable() {

        // Create formatted output for January 1st, 1970 00:00:00
        val expectedDate = SimpleDateFormat( "MMM dd yyyy hh:mm:ss" ).format( Date(0) )

        // Run CLI command
        main(arrayOf("-l", "-h", "-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}single-file-check"))

        /* Parse output
         *
         * Read return of ls, get first element (first line) and split it for arguments
         */
        val result = File(OUTPUT_LOG).readLines()[0].split("\t")

        // Check each file's entry
        assertEquals("rw-", result[0], "Mismatch between expected and actual permissions in rwx")
        assertEquals("1.0 KB", result[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDate, result[2], "Mismatch between expected and actual date")
        assertEquals("single-file-check", result[3], "Mismatch between expected and actual file name")
    }


    @Test
    @DisplayName("should correctly sort elements in the short form")
    fun checkShortFormSorting() {

        // Run CLI command
        main(arrayOf("-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}directory-files"));

        // Read log entry
        val result = File(OUTPUT_LOG).readLines()

        assertEquals("beauty-file", result[0], "Incorrect sorted position of the first element: great")
        assertEquals("charm-file",  result[1], "Incorrect sorted position of the first element: memes")
        assertEquals("strange-file",   result[2], "Incorrect sorted position of the first element: peter")
        assertEquals("truth-file",  result[3], "Incorrect sorted position of the first element: the")

    }

    @Test
    @DisplayName("should correctly reverse sort elements in the short form")
    fun checkShortFormReversedSorting() {

        // Run CLI command
        main( arrayOf("-r", "-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}directory-files") )

        // Read log entry
        val result = File(OUTPUT_LOG).readLines()

        assertEquals("truth-file",  result[0], "Incorrect reverse sorted position of the first element: the")
        assertEquals("strange-file",   result[1], "Incorrect reverse sorted position of the first element: peter")
        assertEquals("charm-file",  result[2], "Incorrect reverse sorted position of the first element: memes")
        assertEquals("beauty-file", result[3], "Incorrect reverse sorted position of the first element: great")
    }


    @Test
    @DisplayName("should correctly list long form of the files")
    fun checkFilesLongForm() {

        main( arrayOf("-l", "-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}directory-files") )

        val result = File(OUTPUT_LOG).readLines()

        val resultFileFirst = result[0].split("\t")
        val resultFileSecond= result[1].split("\t")
        val resultFileThird = result[2].split("\t")
        val resultFileFourth= result[3].split("\t")

        val expectedDateFirst  = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(60))
        val expectedDateSecond = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(120))
        val expectedDateThird  = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(180))
        val expectedDateFourth = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(240))


        assertEquals("010", resultFileFirst[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("2048", resultFileFirst[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateFirst, resultFileFirst[2], "Mismatch between expected and actual date")
        assertEquals("beauty-file", resultFileFirst[3], "Mismatch between expected and actual file name")

        assertEquals("110", resultFileSecond[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("3072", resultFileSecond[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateSecond, resultFileSecond[2], "Mismatch between expected and actual date")
        assertEquals("charm-file", resultFileSecond[3], "Mismatch between expected and actual file name")

        assertEquals("100", resultFileThird[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("4096", resultFileThird[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateThird, resultFileThird[2], "Mismatch between expected and actual date")
        assertEquals("strange-file", resultFileThird[3], "Mismatch between expected and actual file name")

        assertEquals("110", resultFileFourth[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("5120", resultFileFourth[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateFirst, resultFileFourth[2], "Mismatch between expected and actual date")
        assertEquals("truth-file", resultFileFourth[3], "Mismatch between expected and actual file name")
    }


    @Test
    @DisplayName("should correctly list long form of the files in human-readable format")
    fun checkFilesLongFormHumanReadable() {

        main( arrayOf("-l", "-h", "-o", OUTPUT_LOG, "${PLAYGROUND_DIRECTORY}directory-files") )

        val result = File(OUTPUT_LOG).readLines()

        val resultFileFirst = result[0].split("\t")
        val resultFileSecond= result[1].split("\t")
        val resultFileThird = result[2].split("\t")
        val resultFileFourth= result[3].split("\t")

        val expectedDateFirst  = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(60))
        val expectedDateSecond = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(120))
        val expectedDateThird  = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(180))
        val expectedDateFourth = SimpleDateFormat("MMM dd yyyy hh:mm:ss").format(Date(240))


        assertEquals("-w-", resultFileFirst[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("2.0 KB", resultFileFirst[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateFirst, resultFileFirst[2], "Mismatch between expected and actual date")
        assertEquals("beauty-file", resultFileFirst[3], "Mismatch between expected and actual file name")

        assertEquals("rw-", resultFileSecond[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("3.0 KB", resultFileSecond[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateSecond, resultFileSecond[2], "Mismatch between expected and actual date")
        assertEquals("charm-file", resultFileSecond[3], "Mismatch between expected and actual file name")

        assertEquals("r--", resultFileThird[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("4.0 KB", resultFileThird[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateThird, resultFileThird[2], "Mismatch between expected and actual date")
        assertEquals("strange-file", resultFileThird[3], "Mismatch between expected and actual file name")

        assertEquals("rw-", resultFileFourth[0], "Mismatch between expected and actual permissions in bitmask")
        assertEquals("5.0 KB", resultFileFourth[1], "Mismatch between expected and actual file size in units")
        assertEquals(expectedDateFirst, resultFileFourth[2], "Mismatch between expected and actual date")
        assertEquals("truth-file", resultFileFourth[3], "Mismatch between expected and actual file name")
    }
}