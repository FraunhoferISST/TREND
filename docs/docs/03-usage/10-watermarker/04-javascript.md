---
title: JavaScript
---

<!--
 Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.

 This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 that can be found in the LICENSE file.
-->

import PatentHint from './../../patent-hint.mdx'

<PatentHint components={props.components} />

# JavaScript

If you want to use watermarking inside your JavaScript project, this page gives you the necessary
information.

## Compile the library
See [Installation](../installation).


## Plain JavaScript

In order to use this library in plain JavaScript, follow these steps:

1. Build the library ([see above](#compile-the-library))
2. Import the generated JS library:
   ```Html
   <script src='[relative-path-to]/TREND/watermarker/build/dist/js/productionExecutable/watermarker.js'></script>
   ```
3. This generates a variable watermarker that provides access to the library functionalities.
   However, the actual watermarker object is behind the package path and you might want to reassign
   the watermarker variable to get rid of the long path like this:
   ```Javascript
   watermarker = watermarker.de.fraunhofer.isst.trend.watermarker;
   ```
4. Now you are ready to use the watermarker in plain JS. An example HTML file can be found
[here](https://github.com/FraunhoferISST/TREND/blob/main/samples/plain_js/plain_js_minimal_example.html).
You can open the file in the browser, no web server required.

## JavaScript modules

In order to use this library in JavaScript modules, follow these steps:

1. Build the library ([see above](#compile-the-library))
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
   However, the actual watermarker object is behind the package path and you might want to reassign
   the watermarker variable to get rid of the long path like this:
   ```Javascript
   watermarker = watermarker.de.fraunhofer.isst.trend.watermarker; // note that imports in ESM are implicitly constant
   ```
4. Now you are ready to use the watermarker in your module. An example node script can be found
[here](https://github.com/FraunhoferISST/TREND/blob/main/samples/js_modules/js_modules_minimal_example.js).
Run it using `node npm_minimal_example.js`

---
_More follows soon._
