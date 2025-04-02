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

## Compile the Library
See [Installation](01-installation.md).

## Example: Watermarking Text with Text
Below you can see an example project that inserts a text as watermark into a cover text and then
extracts the watermark from the watermarked text.

### 1. Add Library as Dependency
Currently, we are working with local deployment, so you will have to publish the library to 
mavenLocal (see [Installation](01-installation.md)).  

Then you can import it to your local Java project using Gradle ... 
```
dependencies {
    implementation("de.fraunhofer.isst.trend:watermarker:0.1.0-SNAPSHOT")
}
```

... or Maven: 
```
<dependency>
    <groupId>de.fraunhofer.isst.trend</groupId>
    <artifactId>watermarker-jvm</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. Use the Library
The essential methods of the library for inserting and extracting watermarks will generally 
return a Result typed object. Result objects come with a flag that indicates if the operation 
went successfully. They also contain a message field, which may be useful for testing and 
debugging. And of they have a value field, from which the desired resulting payload can be 
retrieved if the operation went successfully (and sometime even, if unsuccessful). The sample 
code below is showing some more details on that. 

*Watermark extraction can be customized for different use cases using optional Boolean parameters
(see [Watermarker](../#extraction-customization) for more details)*

```java title="src/main/java/Main.java" showLineNumbers
import de.fraunhofer.isst.trend.watermarker.Watermarker;
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result;
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // *********************
        // ***** INSERTION *****
        // *********************

        // the coverText to be enhanced with a watermark
        String coverText =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                        "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
                        "volutpat blandit aliquam etiam erat velit.";
        // the watermark text that will be included in the coverText above
        String watermarkText = "Test";

        // prepare watermarker
        Watermarker watermarker = Watermarker.getSingleton();

        // creating a watermark with watermarkText as content
        TextWatermark watermark = TextWatermark.raw(watermarkText);

        // inserting the watermark into the cover text and handling potential errors and warnings
        Result<String> insertionResult = watermarker.textAddWatermark(coverText, watermark);
        String watermarkedText;
        if (insertionResult.isSuccess() && insertionResult.hasValue()) {
            System.out.println("watermarked text:");
            watermarkedText = insertionResult.getValue();
            System.out.println(watermarkedText);
        } else {
            System.out.println("watermarked text not found");
            return;
        }


        // **********************
        // ***** Extraction *****
        // **********************

        // extract the watermark from the watermarked text

        Result<List<TextWatermark>> extractionResultObject = watermarker.textGetTextWatermarks(watermarkedText, true, true, false);
        if (extractionResultObject.isSuccess() && extractionResultObject.hasValue()) {
            if (extractionResultObject.getValue().isEmpty()) {
                System.out.println("No watermarks found");
            } else {
                System.out.println("Found a watermark in the text:");
                extractionResultObject.getValue().forEach(System.out::println);
            }
        }

        // print the watermark text



        // *******************************
        // ***** Multiple watermarks *****
        // *******************************

        // for multiple watermarks in a single text the parameter 'singleWatermark' must be set
        // to 'false' when passed to the extraction function alongside the watermarked text,
        // details are linked above this code block

        // a second Watermark to illustrate multiple watermark extraction
        String secondWatermarkText = "Okay";

        // creating the second watermark
        TextWatermark secondWatermark = TextWatermark.raw(secondWatermarkText);

        // inserting the second watermark into the coverText
        Result<String> secondInsertionResult = watermarker.textAddWatermark(coverText, secondWatermark);
        String secondWatermarkedText;
        if (secondInsertionResult.isSuccess() && secondInsertionResult.hasValue()) {
            System.out.println("watermarked second text:");
            secondWatermarkedText = secondInsertionResult.getValue();
            System.out.println(watermarkedText);
        } else {
            System.out.println("watermarked text not found");
            return;
        }

        // combining the watermarked texts to get two different watermarks in one Text
        String combinedText = watermarkedText + secondWatermarkedText;

        // extract the watermarks from the watermarked text
        // notice that we set the 'singleWatermark' argument to 'false' here:
        Result<List<TextWatermark>> secondExtractionResult = watermarker.textGetTextWatermarks(combinedText, true, false, false);
        if (secondExtractionResult.isSuccess() && secondExtractionResult.hasValue()) {
            List<TextWatermark> extractedMultipleWatermarks = secondExtractionResult.getValue();
            // print the watermarks found
            extractedMultipleWatermarks.forEach(System.out::println);
        } else {
            System.out.println("second watermark extraction failed");
        }
        System.out.println();

        // also notice that it is generally not recommended to concatenate two watermarked texts. this mainly serves
        // to demonstrate that the extraction is capable of identifying several different watermark contents.


        // *******************************
        // ***** Advanced watermarks *****
        // *******************************

        // in the previous sections we were using 'raw' watermarks. Now we will take a look at more advanced types of
        // watermarks that include compression and/or error detection.

        String longerCoverText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

        String longerWatermarkText = "True Blue True Blue True";

        TextWatermark thirdWatermark = TextWatermark.raw(longerWatermarkText);
        Result<String> thirdInsertionResult = watermarker.textAddWatermark(longerCoverText, thirdWatermark);

        // notice that the embedding capacity of the cover text is insufficient for the given 'longerWatermarkText' when
        // using the raw watermark mode
        System.out.println("insert raw success? " + thirdInsertionResult.isSuccess());
        System.out.println("insert raw message: " + thirdInsertionResult.getMessage());
        System.out.println();

        // so we will use compression here:
        thirdWatermark = TextWatermark.compressed(longerWatermarkText);
        thirdInsertionResult = watermarker.textAddWatermark(longerCoverText, thirdWatermark);

        System.out.println("insert compressed success? " + thirdInsertionResult.isSuccess());
        System.out.println("insert compressed hasValue? " + thirdInsertionResult.hasValue());
        System.out.println("insert compressed message: " + thirdInsertionResult.getMessage());
        System.out.println();

        Result<List<TextWatermark>> thirdExtractionResult = watermarker.textGetTextWatermarks(thirdInsertionResult.getValue(), true, true, false);
        System.out.println("third extraction success? " + thirdExtractionResult.isSuccess());
        System.out.println("third extraction hasValue? " + thirdExtractionResult.hasValue());
        System.out.println("third extraction value as expected? " + (longerWatermarkText.equals(thirdExtractionResult.getValue().getFirst().getText())));
        System.out.println();

        // we notice that using compression made the difference for the given combination of cover text and watermark text
        // however it must be said that the efficiency of the compression to some degree depends upon the individual
        // watermark text. I.e. watermark texts that contain repeating patterns (like in the example above) usually yield
        // the best compression results.


        // now let's have a look at the error detection with CRC32

        String anotherWatermarkText = "Lorem ipsum dolore";
        TextWatermark fourthWatermark = TextWatermark.raw(anotherWatermarkText);
        Result<String> fourthInsertionResult = watermarker.textAddWatermark(longerCoverText, fourthWatermark);
        System.out.println("fourth insertion success? " + fourthInsertionResult.isSuccess());
        System.out.println("fourth insertion message: " + fourthInsertionResult.getMessage());

        // we pull the watermarked cover text from the watermarking result:
        String anotherWatermarkedText = fourthInsertionResult.getValue();

        // now let's assume that some part of the watermarked text gets damaged,
        // i.e. we remove 30 characters in the middle of the String:
        anotherWatermarkedText = anotherWatermarkedText.substring(0, 40) + anotherWatermarkedText.substring(70);

        Result<List<TextWatermark>> fourthExtractionResult = watermarker.textGetTextWatermarks(anotherWatermarkedText, true, true, false);
        System.out.println("fourth watermark success? " + fourthExtractionResult.isSuccess());
        System.out.println("fourth watermark hasValue? " + fourthExtractionResult.hasValue());
        System.out.println("fourth watermark found: " + fourthExtractionResult.getValue().getFirst().getText());
        System.out.println("fourth watermark as expected? " + (anotherWatermarkText.equals(fourthExtractionResult.getValue().getFirst().getText())));

        // we notice that the extraction yielded a 'success' result, but the watermark we found is damaged

        System.out.println();

        // so let's try that again with a CRC32-enhanced watermark:
        fourthWatermark = TextWatermark.CRC32(anotherWatermarkText);

        fourthInsertionResult = watermarker.textAddWatermark(longerCoverText, fourthWatermark);
        System.out.println("fourth insertion success? " + fourthInsertionResult.isSuccess());
        System.out.println("fourth insertion message: " + fourthInsertionResult.getMessage());
        System.out.println();

        anotherWatermarkedText = fourthInsertionResult.getValue();
        anotherWatermarkedText = anotherWatermarkedText.substring(0, 40) + anotherWatermarkedText.substring(70);

        fourthExtractionResult = watermarker.textGetTextWatermarks(anotherWatermarkedText, true, true, false);
        System.out.println("fourth watermark success? " + fourthExtractionResult.isSuccess());
        System.out.println("fourth watermark hasValue? " + fourthExtractionResult.hasValue());
        System.out.println("fourth watermark found: " + fourthExtractionResult.getValue().getFirst().getText());
        System.out.println("fourth watermark as expected? " + (anotherWatermarkText.equals(fourthExtractionResult.getValue().getFirst().getText())));

        // we notice two things:
        // - we still only get to see a damaged watermark (which is not the same as the one that was originally put in)
        // - but the 'success' message from the extraction result signaled 'false' which indicates that the watermark
        // must have been damaged in the process

    }

}
```
