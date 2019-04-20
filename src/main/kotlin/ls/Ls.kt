package ls

import org.apache.commons.cli.CommandLine

/* Ls object to working with inodes and configure final output. */
class Ls (private val parsedLine: CommandLine ) {

    /* Ls util options like booleans to easier work with them
     *
     * TODO: output to file
     */
    var long = parsedLine.hasOption("l")
    var humanReadable = parsedLine.hasOption("h")
    var reverse = parsedLine.hasOption("r")


    /* Function to configure output for received inodes from InodeCollector object */
    fun printFiles() {

        /* Create object InodeCollector for getting inodes in current/specified directory */
        val inodeCollector = InodeCollector()

        /*
         * We can get the specified path if parsed argList from args.
         * By default, this value converted to string, looks like this: "[/path/to/directory]" or just like "[]" if no path was given
         * So we convert argList to string and then remove prefix ("[") and suffix ("]")
         *
         * TODO: check for correct arguments
         */
        var pathToFile = parsedLine.argList.toString().removeSurrounding("[", "]")

        /* If path to directory (pathToFile) is not specified -> list files from current repository ("./") */
        if ( pathToFile == "" ) { pathToFile = "./" }

        /*
         * Get inodes from InodeCollector
         *
         * TODO: sort for inodes
         * TODO: reverse option
         */
        val inodes = inodeCollector.collect(pathToFile)


        /* If -l (--long) argument were specified -> output in long format */
        if (long) { printFilesLongFormat(inodes) }
        else { printFilesShortFormat(inodes) }
    }

    /*
     * Simple output for ls
     *
     * TODO: output with right align
     */
    fun printFilesShortFormat(filesInfo: MutableMap<String, Inode>) {
        filesInfo.forEach { (fileName, _) ->

            println(fileName)
        }
    }

    /* Detailed output for ls with --long option */
    fun printFilesLongFormat(filesInfo: MutableMap<String, Inode>) {

        filesInfo.forEach { (fileName, fileInfo) ->

            /* Print size of file, last edited time, name
             *
             * TODO: human-readable output option
             */
            println(fileInfo.size.toString() + "  " + fileInfo.lastEditedTime.toString() + "  " + fileName)
        }
    }
}