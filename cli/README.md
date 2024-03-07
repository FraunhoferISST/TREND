# TREND: CLI

The command line tool (CLI) can be used to add watermarks directly via a shell. It uses the TREND
watermarker library.

## Getting Started

### System prerequisites

The following things are needed to run this application:

- A Java Runtime Environment (JRE)
- The watermarker library, published in maven local(*)

(*) To publish the watermarker library to your maven local repository (if not already done), execute
the following commands from the root directory of the project:

1. `cd watermarker`
2. `./gradlew publishToMavenLocal`

### Building from source

Use Gradle to build the CLI tool:

*Note:* You must replace `<version>` in the following commands (e.g. `0.1.0-SNAPSHOT`)

1. `cd cli` (if not already there)
2. `./gradlew shadowJar"`
    - This will create a standalone jar file: `./build/libs/cli-<version>-all.jar`
3. `java -jar build/libs/cli-<version>-all.jar --help`
    - This will print all possible commands of the CLI tool.
4. *(Optional)* Create alias `trend` to run the CLI tool:
    - Fish: `alias -s trend "java -jar $PWD/build/libs/cli-<version>-all.jar"`
    - Zsh / Bash:
        - *Note:* You must replace `<path/to/cli>` and `<version` in the following commands
        - add the following line to your `~/.zshrc` or `~/.bashrc`:\
          `alias trend='java -jar <path/to/cli>/build/libs/cli-<version>-all.jar'`

### Usage Example

- List all watermarks contained in a file:\
  `trend list example.watermarked.txt`
- *Create a new file from the source file:*
    - Add a watermark to a text file:\
      `trend add "<watermark>" example.txt example.watermarked.txt`
    - Remove all watermarks contained in a file:\
      `trend remove example.watermarked.txt example.txt`
- *Modify the source file:*
    - Add a watermark to a text file:\
      `trend add "<watermark>" example.txt`
    - Remove all watermarks contained in a file:\
      `trend remove example.watermarked.txt`

### Development Build

Use Gradle to recompile and run the CLI tool:

1. `cd cli` (if not already there)
2. `./gradlew run --args="--help"`
    - This will print all possible commands of the CLI tool. To use it, the `--args` parameter has
      to be changed.
