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

1. Clone the TREND repository
2. `cd TREND/watermarker` (if not already there)
3. `./gradlew publishJvmPublicationToMavenLocal`

### Manual Build: JavaScript

Build the watermarker library:

1. Clone the TREND repository
2. `cd TREND/watermarker` (if not already there)
3. `./gradlew build`

#### Plain JavaScript

In order to use this library in plain JavaScript, follow these steps:

1. Build the library ([see above](#manual-build-javascript))
2. Import the generated JS library:
   ```Html
   <script src='[relative-path-to]/TREND/watermarker/build/dist/js/productionExecutable/watermarker.js'></script>
   ```
3. This generates a variable watermarker that provides access to the library functionalities.
   However, the actual watermarker object is behind the package path and you might want to reassign the
   watermarker variable to get rid of the long path like this:
   ```Javascript
   watermarker = watermarker.de.fraunhofer.isst.trend.watermarker;
   ```
4. Now you are ready to use the watermarker in plain JS. An example HTML file can be found [here](../samples/plain_js/plain_js_minimal_example.html). You can open the file in the browser, no web server required.

#### Javascript modules

In order to use this library in JavaScript modules, follow these steps:

1. Build the library ([see above](#manual-build-javascript))
2. Import the generated JS library according to your module system:

   ```Javascript
   // CommonJS (CJS)
   let watermarker = require('[relative-path-to]/TREND/watermarker/build/dist/js/productionExecutable/watermarker.js'); // built as executable
   // or
   let watermarker = require('[relative-path-to]/TREND/watermarker/build/js/packages/watermarker/kotlin/watermarker.js'); // built as module

   // ECMAScript Modules (ESM)
   import watermarker from '[relative-path-to]/TREND/watermarker/build/dist/js/productionExecutable/watermarker.js'; // built as executable
   // or
   import watermarker from '[relative-path-to]/TREND/watermarker/build/js/packages/watermarker/kotlin/watermarker.js'; // built as module
   ```

3. This generates a variable watermarker that provides access to the library functionalities.
   However, the actual watermarker object is behind the package path and you might want to reassign the
   watermarker variable to get rid of the long path like this:
   ```Javascript
   watermarker = watermarker.de.fraunhofer.isst.trend.watermarker; // note that imports in ESM are implicitly constant
   ```
4. Now you are ready to use the watermarker in your module. An example node script can be found [here](../samples/js_modules/js_modules_minimal_example.js). Run it using `node npm_minimal_example.js`
