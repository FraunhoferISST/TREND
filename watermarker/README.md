# TREND: Watermarker Library

The watermarker library is the hearth of the project because it is the foundation for different
other components. It can be used to embed or extract a watermark directly in or from text.

## Getting Started

Keep in mind that the library isn't a stand-alone application. It can be used in different
applications build with Java or Kotlin (JVM and JS) to embed and extract watermarks. When searching
for a running application, have a look at the CLI or webinterface tool in this project repository.

### System prerequisites

The following things are needed to build this library:

- A Java Runtime Environment (JRE)

### Manual Build: Java / Kotlin for Maven

Build the watermarker library and publish it to Maven local:

1. `cd watermarker` (if not already there)
2. `./gradlew publishJvmPublicationToMavenLocal`
