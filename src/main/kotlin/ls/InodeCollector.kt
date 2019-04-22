package ls

import java.io.File
import java.io.IOException
import java.util.Date
import java.text.SimpleDateFormat
import kotlin.IllegalArgumentException
import kotlin.math.*

/* Object for working with files and their inodes */
class InodeCollector {

    /* Map for filenames and their inodes */
    private val inodes = mutableMapOf<String, Inode>()


    /* Collect information about file's type for coloring output to distinguish file types */
    private fun fileType(file: File): InodeType {

        /* Initialize variable */
        var fileType = InodeType.File

        /* Checks for current file type (directory, file or executable file) */
        if (file.canExecute()) { fileType = InodeType.ExecFile }
        if (file.isDirectory) { fileType = InodeType.Directory }

        return fileType
    }

    /* Collect information about file's size in bytes */
    private fun fileSize(file: File, sizeInBytes: Boolean): String {

        val fileSize = file.length()

        if (sizeInBytes) { return fileSize.toString() }
        else {

            val unit = 1024.toDouble()
            if ( fileSize < unit ) return ("$fileSize B")
            val exponent = log(fileSize.toDouble(), unit).toInt()
            val units = "BKMGTPE"
            val currentUnit = units[exponent].toString()

            return String.format("%.1f %sB", fileSize/unit.pow(exponent), currentUnit)
        }
    }

    /* Collect information about last modification time */
    private fun fileLastModified(file: File): String {

        val fileLastModificationTimeDateObject = Date(file.lastModified())
        val dateFormatter = SimpleDateFormat("MMM dd yyyy hh:mm:ss")

        return dateFormatter.format(fileLastModificationTimeDateObject) ?: throw IOException()
    }

    /* Collect information about file's permissions in "rwx" style */
    private fun filePermissions(file: File, permissionsInBitmask: Boolean): String {

        /* Check file's permissions */
        val filePermissionsWritable = file.canWrite()
        val filePermissionsReadable = file.canRead()
        val filePermissionsExecutable= file.canExecute()

        if (permissionsInBitmask) {

            /* Value in bitmask for "permissions" option */
            return (if (filePermissionsReadable) "1" else "0") +
                    (if (filePermissionsWritable) "1" else "0") +
                    (if (filePermissionsExecutable) "1" else "0")

        } else {

            /* Value in "rwx" style for "permissions" option */
            return (if (filePermissionsReadable) "r" else "-") +
                    (if (filePermissionsWritable) "w" else "-") +
                    (if (filePermissionsExecutable) "x" else "-")
        }
    }

    /* Function for compile information about files in specified directory */
    fun collect(givenFilePath: String,
                sizeOutputFormat: Boolean,
                permissionsOutputFormat: Boolean)
            : MutableMap<String, Inode> {

        /* Create File object. */
        val givenFileObject = File(givenFilePath)

        if (!givenFileObject.canWrite()) { throw IllegalArgumentException("Incorrect path to directory or file") }

        /*
         * If current directory is empty, we will not write anything to inodes Map
         */
        if (givenFileObject.isDirectory and (givenFileObject.length() > 0)) {

            /* Iterate through all files in directory */
            givenFileObject.list().forEach { listedFileName ->

                /* Path for current file to create File object */
                val listedFilePath = "$givenFilePath/$listedFileName"
                /* Create File object for current file */
                val listedFileObject = File(listedFilePath)

                /* Add current file and its inode to inodes */
                inodes[listedFileName] = Inode(
                    fileType(listedFileObject),
                    fileSize(listedFileObject, sizeOutputFormat),
                    fileLastModified(listedFileObject),
                    filePermissions(listedFileObject, permissionsOutputFormat)
                    )
            }
        }
        return inodes
    }
}
