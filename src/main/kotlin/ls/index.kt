package ls

import org.apache.commons.cli.*
import java.lang.IllegalArgumentException


fun main(args: String) {

    /* Create Option object */
    val options = Options()

    /*
     * Add "-l" ("--long" for long format) option for long listing format.
     * Listing files with information about: permissions, last modification time, size of file
     */
    options.addOption("l", "long", false, "use a long listing format")

    /*
     * Add "-h" option for human-readable output.
     * When "-l" argument is given -> print size as an integer and size unit (for example KB or MB), print permissions as rwx
     */
    options.addOption("h", false, "with -l, print human readable sizes")

    /* Add "-r" option for output in reverse order while sorting */
    options.addOption("r", "reverse", false, "reverse order while sorting")

    /* Add "-o" option for redirect output to specified file */
    options.addOption("o", "output", true, "output to specified file")

    options.addOption("", "help", false, "print this help message")

    // Create the parser
    val parser = DefaultParser()

    // Create ls object
    val ls = Ls()




    /* If right arguments were passed */
    try {

        // Parse options
        val line = parser.parse(options, arrayOf(args)) ?: throw IllegalArgumentException()

        if ( line.hasOption("help") ) {
            ls.printHelp(options)
            return
        }

        // Print help if --help option is given
        if ( line.hasOption("help") ) {  }

        // List files to specified file or to console
        ls.printFiles(line)

    /* If wrong arguments were passed and can't be parsed -> print help for ls */
    } catch(e: ParseException) {

        /* Oops, something went wrong, print error description */
        println(e.message + "\n")

        /* Print help message */
        ls.printHelp(options)
    }
}
