package ls

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options

/* Ls object to working with inodes and configure final output. */
class Ls {


    fun printHelp(options: Options) {

        /* Automatically generate the help statement */
        val formatter = HelpFormatter()

        /* Print help information for those who are lost... */
        formatter.printHelp(

            /* Command */
            "ls [-l] [-h] [-r] [-o output.file] directory_or_file\n",
            /* Header for help */
            "List information about the FILEs (the current directory by default).\n",
            /* Options object with ls arguments */
            options,
            /* Footer for help */
            "\n" +
                    "Size units are KB,MB,GB,TB,PB,EB (powers of 1024)."
        )
    }


    /* Function to configure output for received inodes from InodeCollector object */
    fun printFiles( parsedLine: CommandLine ) {

        /* Ls util options like booleans to easier work with them */
        val long = parsedLine.hasOption("l")
        val humanReadable = parsedLine.hasOption("h")
        val reverse = parsedLine.hasOption("r")
        val outputToFile = parsedLine.hasOption("o")

        /* Path to file to redirect output from console */
        val outputToFilePath = parsedLine.getOptionValue("o")

        /* ls util output selection */
        val output = if (outputToFile) { OutputFile(outputToFilePath) }
        else { OutputConsole() }

        /* Create object InodeCollector for getting inodes in current/specified directory */
        val inodeCollector = InodeCollector()

        /*
         * We can get the specified path if parsed argList from args.
         * By default, this value converted to string, looks like this: "[/path/to/directory]" or just like "[]" if no
         * path was given
         * So we convert argList to string and then remove prefix ("[") and suffix ("]")
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
        if (long) { printFilesLongFormat(inodes, output) }
        else { printFilesShortFormat(inodes, output) }

        output.close()
    }

    /*
     * Simple output for ls
     *
     * TODO: output with right align
     */
    private fun printFilesShortFormat(filesInfo: MutableMap<String, Inode>, output: Output) {

        filesInfo.forEach { (filename, _) ->

            output.println(filename)
        }
    }

    /* Detailed output for ls with --long option */
    private fun printFilesLongFormat(filesInfo: MutableMap<String, Inode>, output: Output) {

        filesInfo.forEach { (fileName, fileInfo) ->

            /* Print size of file, last edited time, name */
            output.println (
                fileInfo.permissions + "\t" +
                        fileInfo.size + "\t" +
                        fileInfo.lastEditedTime + "\t" +
                        fileName
            )
        }
    }
}
