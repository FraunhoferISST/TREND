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

## Compile the library
See [Installation](../installation).

## Example: Watermarking Text with Text
Below you can see an example project that inserts a text as watermark into a cover text and then
extracts the watermark from the watermarked text.

---
*Line 12 is the important line that adds our library as dependency into the project. Currently, we
are working with local deployment, so you will have to add mavenLocal() as repo (line 8) and
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
    implementation("de.fraunhofer.isst.trend:watermarker:0.1.0-SNAPSHOT")
}

application {
    mainClass.set("MainKt")
}
```
---

*The extension functions on lines 49 and 71 are optional for easy error handling with our custom return
types (see [Concepts](../../../development/watermarker/concepts/#error-handling-1) for more
details).*
```kt title="src/main/kotlin/Main.kt" showLineNumbers
import de.fraunhofer.isst.trend.watermarker.Watermarker
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark
import kotlin.system.exitProcess

fun main() {
    // prepare data
    val coverText =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
            "volutpat blandit aliquam etiam erat velit."
    val watermarkText = "Test"

    // prepare watermarker
    val watermarker = Watermarker()

    // creating a watermark with text as content
    val watermark = TextWatermark.new(watermarkText)

    // inserting the watermark into the cover text and handling potential errors and warnings
    val watermarkedText = watermarker.textAddWatermark(coverText, watermark).unwrap()

    // print the watermarked text
    println("watermarked text:")
    println(watermarkedText)
    println()

    // extract the watermark from the watermarked text
    val extractedWatermarks = watermarker.textGetTextWatermarks(watermarkedText).unwrap()
    check(extractedWatermarks.size == 1)
    val extractedWatermark = extractedWatermarks[0]

    // print the watermark text
    println("Found a watermark in the text:")
    println(extractedWatermark.text)
    
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
    if (isSuccess) {
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
        "A Result with a Status of type Success or Warning are expected to have a value"
    }

    return value!!
}
```
---
_More follows soon._

