---
title: Installation
---

<!--
 Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.

 This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 that can be found in the LICENSE file.
-->

import PatentHint from './../../patent-hint.mdx'

<PatentHint components={props.components} />

# Installation

## System Requirements

**Java** is required to build the library with [Gradle](https://gradle.org/).

## Building from Source
1. Clone the TREND repository
2. Go into the directory of the watermarker: `cd TREND/watermarker`
3. Build the library: `./gradlew build`
4. Publish the artifacts locally: `./gradlew publishToMavenLocal`
