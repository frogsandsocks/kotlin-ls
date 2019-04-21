package ls

import org.apache.commons.cli.CommandLine

/* Ls object to working with inodes and configure final output. */
class Ls (private val parsedLine: CommandLine ) {

    /* Ls util options like booleans to easier work with them */
    private var long = parsedLine.hasOption("l")
    var humanReadable = parsedLine.hasOption("h")
    private var reverse = parsedLine.hasOption("r")
    private val outputToFile = parsedLine.hasOption("o")
    private val outputToFilePath = parsedLine.getOptionValue("o")

    private val output = if (outputToFile) { OutputFile(outputToFilePath) }
    else { OutputConsole() }


    /* Function to configure output for received inodes from InodeCollector object */
    fun printFiles() {

        /* Create object InodeCollector for getting inodes in current/specified directory */
        val inodeCollector = InodeCollector()

        /*
         * We can get the specified path if parsed argList from args.
         * By default, this value converted to string, looks like this: "[/path/to/directory]" or just like "[]" if no
         * path was given
         * So we convert argList to string and then remove prefix ("[") and suffix ("]")
         *
         * TODO: check for correct arguments
         */
        var pathToFile = parsedLine.argList.toString().removeSurrounding("[", "]")

        /* If path to directory (pathToFile) is not specified -> list files from current repository ("./") */
        if ( pathToFile == "" ) { pathToFile = "./" }


        /* Flags for InodeCollector */
        /* File's size in bytes or in units */
        var fileSizeInBytes = true

        /* File's permissions in bitmask or in rwx */
        var filePermissionsInBitmask = true

        /* If "human readable" option is given -> list file's size in units and permissions in rwx */
        if (humanReadable) {

            fileSizeInBytes = false
            filePermissionsInBitmask = false
        }

        /*
         * Get inodes from InodeCollector.
         * Sort inodes for ascending or descending order
         */
        var inodes = inodeCollector.collect(pathToFile, fileSizeInBytes, filePermissionsInBitmask).toSortedMap()
        if (reverse) { inodes = inodes.toSortedMap(Comparator.reverseOrder()) }

        /* If -l (--long) argument were specified -> output in long format */
        if (long) { printFilesLongFormat(inodes) }
        else { printFilesShortFormat(inodes) }

        output.closeFile()
    }

    /*
     * Simple output for ls
     *
     * TODO: output with right align
     */
    private fun printFilesShortFormat(filesInfo: MutableMap<String, Inode>) {

        filesInfo.forEach { (filename, _) ->

            output.println(filename)
        }
    }

    /* Detailed output for ls with --long option */
    private fun printFilesLongFormat(filesInfo: MutableMap<String, Inode>) {

        filesInfo.forEach { (fileName, fileInfo) ->

            /* Print size of file, last edited time, name */
            output.println (
                fileInfo.permissions + "\t" +
                    fileInfo.size.toString() + "\t" +
                    fileInfo.lastEditedTime.toString() + "\t" +
                    fileName
            )
        }
    }
}
