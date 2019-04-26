package tests

import ls.InodeCollector
import org.junit.jupiter.api.Assertions.*
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
internal class InodeCollectorTest {

    companion object {

        private var PLAYGROUND_DIRECTORY_PATH = ""

        @BeforeAll
        @JvmStatic
        internal fun createPlayground(@TempDir tempDir : Path) {

            /* Set the playground path */
            PLAYGROUND_DIRECTORY_PATH = tempDir.toAbsolutePath().toString() + "/"


            /* Create empty directory */
            File(PLAYGROUND_DIRECTORY_PATH + "directory-empty").mkdir()

            /* Create directory with files for listing test */
            File(PLAYGROUND_DIRECTORY_PATH + "directory-files").mkdir()
            File(PLAYGROUND_DIRECTORY_PATH + "directory-files/beauty-file").createNewFile()
            File(PLAYGROUND_DIRECTORY_PATH + "directory-files/charm-file").createNewFile()
            File(PLAYGROUND_DIRECTORY_PATH + "directory-files/strange-file").createNewFile()
            File(PLAYGROUND_DIRECTORY_PATH + "directory-files/truth-file").createNewFile()


            /* Create file for name test */
            File(PLAYGROUND_DIRECTORY_PATH + "file-name-test").createNewFile()

            /* Create file for modify date test */
            val modifyDateFile = File(PLAYGROUND_DIRECTORY_PATH + "file-modify-date-test")
            modifyDateFile.createNewFile()
            modifyDateFile.setLastModified(0)

            /* Create file for file-size test */
            val sizeTestFile = File(PLAYGROUND_DIRECTORY_PATH + "file-size-test")
            sizeTestFile.createNewFile()

            /* Fill size test file with random bytes */
            val randomInstance = Random()
            val sizeTestContent = ByteArray(1024)
            randomInstance.nextBytes(sizeTestContent)
            sizeTestFile.writeBytes(sizeTestContent)

            // Create file for permissions test (which is not supported on Windows)
            if (System.getProperty("os.name").toLowerCase() !== "windows") {

                // Create file for permissions test
                val permissionTestFile = File(PLAYGROUND_DIRECTORY_PATH + "file-permissions-test")
                permissionTestFile.createNewFile()

                permissionTestFile.setExecutable(true)

                /*
                // Create PosixFilePermission implementation for 755 (rwxrx-rx-)
                val perms = HashSet<PosixFilePermission>()
                perms.add(PosixFilePermission.OWNER_READ)
                perms.add(PosixFilePermission.OWNER_WRITE)
                perms.add(PosixFilePermission.OWNER_EXECUTE)
                perms.add(PosixFilePermission.GROUP_READ)
                perms.add(PosixFilePermission.GROUP_EXECUTE)
                perms.add(PosixFilePermission.OTHERS_READ)
                perms.add(PosixFilePermission.OTHERS_EXECUTE)

                // Apply permissions
                Files.setPosixFilePermissions(permissionTestFile.toPath(), perms)
                */
            }
        }
    }

    @Test
    @DisplayName("should fail on non-existing file")
    fun checkNonExistingFile() {

        assertThrows(IllegalArgumentException::class.java) {

            val inodeCollector = InodeCollector()
            inodeCollector.collect(
                givenFilePath = PLAYGROUND_DIRECTORY_PATH + "not-existing-directory",
                sizeInBytes = false,
                permissionsInBitmask = false
            )
        }
    }

    @Test
    @DisplayName("should correctly list empty directory")
    fun checkEmptyDirectory() {

        val listing = InodeCollector().collect(
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "directory-empty",
            sizeInBytes = false,
            permissionsInBitmask = false
        )

        assertEquals (
            1,
            listing.count(),
            "There should be only one inode for empty directory"
        )

        assertEquals (
            "rwx",
            listing["directory-empty"]?.permissions,
            "Permissions must be u+rwx, because it's directory created by us"
        )
    }

    @Test
    @DisplayName("should list files in directory")
    fun checkDirectoryListing() {

        val listing = InodeCollector().collect(
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "directory-files",
            sizeInBytes = false,
            permissionsInBitmask = false
        )

        assertNotNull(listing["beauty-file"], "Couldn't find file: beauty-file")
        assertNotNull(listing["charm-file"], "Couldn't find file: charm-file")
        assertNotNull(listing["strange-file"], "Couldn't find file: strange-file")
        assertNotNull(listing["truth-file"], "Couldn't find file: truth-file")
    }


    @Test
    @DisplayName("should correctly detect file's name")
    fun checkFileName() {

        val listing = InodeCollector().collect (
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "file-name-test",
            sizeInBytes = false,
            permissionsInBitmask = false
        )

        assertNotNull(listing["file-name-test"], "Can't find expected test file name in listing")
    }

    @Test
    @DisplayName("should correctly detect modify date")
    fun checkModifyDate() {

        // Create formatted output for January 1st, 1970 00:00:00 UTC
        val expectedDate = SimpleDateFormat( "MMM dd yyyy hh:mm:ss" ).format( Date(0) )

        // Collecting inode with special timestamp
        val listing = InodeCollector().collect (
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "file-modify-date-test",
            sizeInBytes = false,
            permissionsInBitmask = false
        )

        // Checking equality
        assertEquals (
            expectedDate,
            listing["file-modify-date-test"]?.lastEditedTime,
            "Mismatch between expected and actual last modify timestamps"
        )
    }


    @Test
    @DisplayName("should correctly detect file size in bytes")
    fun checkFileSize() {

        // Getting listing with special file containing exactly 1Kb of random, without size formatting
        val listing = InodeCollector().collect (
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "file-size-test",
            sizeInBytes = true,
            permissionsInBitmask = false
        )

        // Checking equality
        assertEquals (
            "1024",
            listing["file-size-test"]?.size,
            "Mismatch between expected and actual file size in bytes"
        )
    }

    @Test
    @DisplayName("should correctly format file size")
    fun checkFileSizeFormatting() {

        // Getting listing with special file containing exactly 1Kb of random, with size formatting
        val listing = InodeCollector().collect (
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "file-size-test",
            sizeInBytes = false,
            permissionsInBitmask = false
        )

        // Checking equality
        assertEquals(
            "1.0 KB",
            listing["file-size-test"]?.size,
            "Mismatch between expected and actual formatted file size"
        )
    }


    @Test
    @DisplayName("should correctly detect file permissions in human-readable format")
    fun checkFilePermissions() {

        // Getting listing with special permissions (u+rwx / 755) with permission representation in human-readable format
        val listing = InodeCollector().collect(
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "file-permissions-test",
            sizeInBytes = false,
            permissionsInBitmask = false
        )

        // Checking equality
        assertEquals (
            "rwx",
            listing["file-permissions-test"]?.permissions,
            "Mismatch between expected and actual human-readable permissions representation"
        )
    }


    @Test
    @DisplayName("should correctly detect file permissions in bitmask format")
    fun checkFormattedFilePermissions() {

        // Getting listing with special permissions (u+rwx / 755) with permission representation in human-readable format
        val listing = InodeCollector().collect (
            givenFilePath = PLAYGROUND_DIRECTORY_PATH + "file-permissions-test",
            sizeInBytes = false,
            permissionsInBitmask = true
        )

        // Checking equality
        assertEquals (

            "111",
            listing["file-permissions-test"]?.permissions,
            "Mismatch between expected and actual bitmask permissions representation"
        )
    }
}
