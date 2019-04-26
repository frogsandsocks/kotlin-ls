# kotlin-ls

University practical task. Util for listing files from specified directory. Just like normal ls, but for minimalists and limited with just four arguments :baby_chick:

## Usage

Command Line: ls [-l] [-h] [-r] [-o output.file] directory_or_file

When executed without any arguments — list files' names in current directory

### Arguments

| Argument | Description |
| --- | --- |
| -l, --long | listing files in long format (with permissions for current user in bitmask, size in bytes and last moddified time) |
| -h | when with -l, print human readable sizes (e.g., 1K 234M 2G) and permissions in rwx |
| -r, --recursive | list subdirectories recursively |
| -o, --output | redirect output to specified file |
| --help | print help message |


### Prerequisites

I used in this project Commons CLI library to parsing arguments.

Library was installed by adding this line to the build.gradle scipt in dependences {}

For tests I used junit 5.5.0-M1
