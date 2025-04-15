---
title: Kotlin
---

<!--
 Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.

 This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 that can be found in the LICENSE file.
-->

import PatentHint from './../../patent-hint.mdx'

<PatentHint components={props.components} />

# Kotlin
If you want to use watermarking inside your Kotlin project, this page gives you the necessary
information.

## Compile the Library
See [Installation](../installation).

## Example: Watermarking Text with Text
Below you can see an example project that inserts a text as a watermark into a cover text and then
extracts the watermark from the watermarked text.

### 1. Add Library as Dependency
*Line 12 is the important line that adds our library as a dependency into the project. Currently, we
are working with local deployment, so you will have to add `mavenLocal()` as repo (line 8) and
publish the library to mavenLocal (see [Installation](../installation)).*
```kt title="build.gradle.kts" showLineNumbers
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    application
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("de.fraunhofer.isst.innamark:watermarker:0.1.0-SNAPSHOT")
}

application {
    mainClass.set("MainKt")
}
```

### 2. Use the Library
*The extension functions `handle()` and `unwrap()` are optional for easy error handling with our
custom return types (see [Concepts](../../../development/watermarker/concepts/#error-handling-1)
for more details).*

*Watermark extraction can be customized for different use cases using optional Boolean parameters
(see [Watermarker](../#extraction-customization) for more details)*

```kt title="src/main/kotlin/Main.kt" showLineNumbers
import de.fraunhofer.isst.innamark.watermarker.Watermarker
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import kotlin.system.exitProcess

fun main() {
    // *********************
    // ***** INSERTION *****
    // *********************

    // the coverText to be enhanced with a watermark
    val coverText =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
            "volutpat blandit aliquam etiam erat velit."
    // the watermark text that will be included in the coverText above
    val watermarkText = "Test"

    // prepare watermarker
    val watermarker = Watermarker()

    // creating a watermark with watermarkText as content
    val watermark = TextWatermark.new(watermarkText)

    // inserting the watermark into the cover text and handling potential errors and warnings
    val watermarkedText = watermarker.textAddWatermark(coverText, watermark).unwrap()

    // print the watermarked text
    println("watermarked text:")
    println(watermarkedText)
    println()
    
    // **********************
    // ***** Extraction *****
    // **********************

    // extract the watermark from the watermarked text
    val extractedWatermarks = watermarker.textGetTextWatermarks(watermarkedText).unwrap()
    check(extractedWatermarks.size == 1)
    val extractedWatermark = extractedWatermarks[0]

    // print the watermark text
    println("Found a watermark in the text:")
    println(extractedWatermark.text)


    // *******************************
    // ***** Multiple watermarks *****
    // *******************************

    // for multiple watermarks in a single text an additional parameter 'singleWatermark = false'
    // can be passed to the extraction function alongside the watermarked text, details are linked 
    // above this code block 

    // a second Watermark to illustrate multiple watermark extraction
    val secondWatermarkText = "Okay"

    // creating the second watermark
    val secondWatermark = TextWatermark.new(secondWatermarkText)

    // inserting the second watermark into the coverText
    val secondWatermarkedText = watermarker.textAddWatermark(coverText, secondWatermark).unwrap()

    // combining the watermarked texts to get two different watermarks in one Text
    val combinedText = watermarkedText + secondWatermarkedText

    // extract the watermarks from the watermarked text
    val extractedMultipleWatermarks =
        watermarker.textGetTextWatermarks(combinedText, singleWatermark = false).unwrap()

    // print the watermarks found
    for (extracted in extractedMultipleWatermarks) println("Found watermark: $extracted")

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
fun Status.handle() {
    if (isSuccess && !this.hasCustomMessage) {
        return
    }

    println(this)

    if (isError) {
        exitProcess(-1)
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
fun <T> Result<T>.unwrap(): T {
    status.handle()
    checkNotNull(value) {
        "A Result with a Status of type Success or Warning is expected to have a value"
    }

    return value!!
}
```
