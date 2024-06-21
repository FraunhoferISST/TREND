---
title: Java
---

<!--
 Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.

 This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 that can be found in the LICENSE file.
-->

import PatentHint from './../../patent-hint.mdx'

<PatentHint components={props.components} />

# Java

If you want to use watermarking inside your Java project, this page gives you the necessary
information.

## Compile the library
See [Installation](../installation).

## Example: Watermarking Text with Text
Below you can see an example project that inserts a text as watermark into a cover text and then
extracts the watermark from the watermarked text.

---
*Line 11 is the important line that adds our library as dependency into the project. Currently, we
are working with local deployment, so you will have to add mavenLocal() as repo (line 8) and
publish the library to mavenLocal (see [Installation](../installation)).*
```kt title="build.gradle.kts" showLineNumbers
plugins {
    application
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("de.fraunhofer.isst.trend:watermarker:0.1.0-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("Main")
}
```
---

*The static functions on lines 52 and 74 are optional for easy error handling with our custom return
types (see [Concepts](../../../development/watermarker/concepts/#error-handling-1) for more
details).*
```java title="src/main/java/Main.java" showLineNumbers
import de.fraunhofer.isst.trend.watermarker.Watermarker;
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result;
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status;
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        // prepare data
        String coverText =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                        "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
                        "volutpat blandit aliquam etiam erat velit.";
        String watermarkText = "Test";

        // prepare watermarker
        Watermarker watermarker = new Watermarker();

        // creating a watermark with text as content
        TextWatermark watermark = TextWatermark.Companion.create(watermarkText);

        // inserting the watermark into the cover text and handling potential errors and warnings
        String watermarkedText = unwrap(watermarker.textAddWatermark(coverText, watermark));

        // print the watermarked text
        System.out.println("watermarked text:");
        System.out.println(watermarkedText);
        System.out.println();

        // extract the watermark from the watermarked text
        List<TextWatermark> extractedWatermarks =
                unwrap(watermarker.textGetTextWatermarks(watermarkedText, true, false));
        assert(extractedWatermarks.size() == 1);
        TextWatermark extractedWatermark = extractedWatermarks.get(0);

        // print the watermark text
        System.out.println("Found a watermark in the text:");
        System.out.println(extractedWatermark.getText());
    }

    /**
     * Handles a status depending on its variant:
     * Variant Error:
     *  - print error and exit with code -1
     * Variant Warning:
     *  - print warning
     * Variant Success:
     *  - nop
     */
    public static void handle(Status status) {
        if (status.isSuccess() && !status.getHasCustomMessage()) {
            return;
        }

        System.out.println(status);

        if (status.isError()) {
            System.exit(-1);
        }
    }

    /**
     * Unwraps a Result depending on its variant:
     * Variant Error:
     *  - print error and exit with code -1
     * Variant Warning:
     *  - print warning
     *  - return non-null value
     * Variant Success:
     *  - return non-null value
     */
    public static <T> T unwrap(Result<T> result) {
        handle(result.getStatus());
        assert result.getHasValue() :
                "A Result with a Status of type Success or Warning are expected to have a value";

        return result.getValue();
    }
}
```

---
_More follows soon._
