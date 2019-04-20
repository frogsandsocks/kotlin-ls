package ls

import java.io.File

/* Object for working with files and their inodes */
class InodeCollector {

    /* Map for filenames and their inodes */
    private val inodes = mutableMapOf<String, Inode>()

    /* Function for compile information about files in specified directory */
    fun collect(givenFilePath: String): MutableMap<String, Inode> {

        /* Create File object. */
        val givenFileObject = File(givenFilePath)

        /*
         * If current directory is empty, we will not write anything to inodes Map
         * TODO: check and exception for path to file, not to repository
         */
        if (givenFileObject.isDirectory and (givenFileObject.length() > 0)) {

            /* Iterate through all files in directory */
            givenFileObject.list().forEach { listedFileName ->

                /* Path for current file to create File object */
                val listedFilePath = "$givenFilePath/$listedFileName"
                /* Create File object for current file */
                val listedFileObject = File(listedFilePath)

                /* Initialize variable */
                var listedFileType = InodeType.File

                /* Checks for current file type (directory, file or executable file) */
                if (listedFileObject.canExecute()) { listedFileType = InodeType.ExecFile }
                if (listedFileObject.isDirectory) { listedFileType = InodeType.Directory }

                /* Values for options "size" and "last edited time" */
                val listedFileLastModificationTime = listedFileObject.lastModified()
                val listedFileSize = listedFileObject.length()

                /* Add current file and its inode to inodes */
                inodes[listedFileName] = Inode(listedFileType, listedFileLastModificationTime, listedFileSize)
            }
        }
        return inodes
    }

    /*
     * TODO: implement capability to return inodes with another "size" attribute and "rules" attribute format
     */
}
