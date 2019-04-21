package ls

import org.apache.commons.cli.*
import java.lang.IllegalArgumentException


fun main(args: Array<String>) {

    /* Create Option object */
    val options = Options()

    /*
     * Add "-l" ("--long" for long format) option for long listing format.
     * Listing files with information about: rules, last modification time, size of file
     */
    options.addOption("l", "long", false, "use a long listing format")

    /*
     * Add "-h" option for human-readable output.
     * When "-l" argument is given -> print size as an integer and size unit (for example KB or MB), print rules as rwx
     */
    options.addOption("h", false, "with -l, print human readable sizes")

    /* Add "-r" option for output in reverse order while sorting */
    options.addOption("r", "reverse", false, "reverse order while sorting")

    /* Add "-o" option for redirect output to specified file */
    options.addOption("o", "output", true, "output to specified file")


    // Create the parser
    val parser = DefaultParser()

    /* If right arguments were passed */
    try {

        // Parse options
        val line = parser.parse(options, args) ?: throw IllegalArgumentException()

        // Create ls object with the line with options
        val ls = Ls(line)

        // List files to specified file or to console
        ls.printFiles()

    /* If wrong arguments were passed and can't be parsed -> print help for ls */
    } catch(e: ParseException) {

        /* Oops, something went wrong, print error description */
        println(e.message + "/n")

        /* Automatically generate the help statement */
        val formatter = HelpFormatter()

        /* Print help information for those who are lost... */
        formatter.printHelp(

            /* Command */
            "ls [OPTIONS]... [FILE]...\n",
            /* Header for help */
            "List information about the FILEs (the current directory by default).\n",
            /* Options object with ls arguments */
            options,
            /* Footer for help */
            "\n" +
                    "The SIZE argument is an integer and optional unit (example: 10K is 10*1024).\n" +
                    "Units are K,M,G,T,P,E,Z,Y (powers of 1024) or KB,MB,... (powers of 1000)."
        )
    }
}
