# Contributing to the Project

Thank you for your interest in contributing to the TREND Watermarker!

## Table of contents

* [CLA](#cla)
* [Issues and Pull Requests](#issues-and-pull-requests)
* [License Header](#license-header)
* [Open Project in IntelliJ](#open-project-in-intellij)
* [Style Guide](#style-guide)
    * [IntelliJ IDEA configuration](#intellij-idea-configuration)
    * [Git](#Git)

## CLA

When contributing to this project, the _Corporate Contributor License Agreement (“CLA”)_ must be
accepted. See the `CLA.md` file for more information.

## Issues and Pull Requests

To organize and structure all contributions, open an issue for enhancements, feature requests or
bugs. In case of security issues, please see `SECURITY.md` for more details.

Code changes are handled entirely over pull requests. We use
the [squash and merge](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/about-pull-request-merges#squash-and-merge-your-commits)
strategy. Therefore, remember to use a pull request title in the
[conventional commits](https://www.conventionalcommits.org/) format since it is used as the squashed
commit message.

## License Header

Every source code file in this project must contain a license header. Example:

```
/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
```

## Open Project in IntelliJ

To work on all subprojects from one IntelliJ instance, you may have to manually add
the `build.gradle.kts` files.

- If the Gradle tool window is missing in IntelliJ and is not listen in "View -> Tool Windows ->
  Gradle" open one of the `build.gradle.kts` files in a subproject (e.g. watermaker). IntelliJ
  should offer a button "Link Gradle project". After clicking on it IntelliJ will open a file
  selector window with the `build.gradle.kts` selected. Press "OK". The Gradle tool window should
  open
- In the gradle tool window press the "+" button and add a `build.gradle.kts` from another
  subproject. Repeat until all projects are added
- Gradle will start to download dependencies and build all projects. If something does not work, you
  can manually trigger a reload by clicking on the reload button (circle with two arrows)
- Once Gradle finished loading the projects, everything should be working from here on

## Style Guide

We aim for a coherent and consistent code base, thus the coding style detailed here should be
followed.
Therefore, we follow the

- [Kotlin Coding convetions](https://kotlinlang.org/docs/coding-conventions.html)
- [KDoc syntax](https://kotlinlang.org/docs/kotlin-doc.html)

with minor adjustments:

- Hard wrap at: 100
- KDoc max comment line length: 100

### Autoformatter

To ensure that these style guides are followed we advise to use an autoformatter.

#### IntelliJ IDEA configuration

The code style should be shipped in the `.idea` folder. Otherwise, configure it manually:

- Apply Kotlin style guide
    - Settings -> Editor -> Code Style -> Kotlin -> Set from ... -> Kotlin style guide
    - [Source](https://kotlinlang.org/docs/coding-conventions.html)
- Hard wrap at: 100
    - Settings -> Editor -> Code Style -> Kotlin -> Wrapping and Braces
        - Hard wrap at: 100
        - Wrap on typing: yes
- Tabs and Indents: 4
    - Settings -> Editor -> Code Style -> Kotlin -> Tabs and Indents
        - Tab size: 4
        - Indent: 4
        - Continuation indent: 4
- Single Name Import:
    - Settings -> Editor -> Code Style -> Kotlin -> Imports
        - Top-Level Symbols: Use single name import
        - Java Statics and Enum members: Use single name import
- Actions on Save:
    - Settings -> Tools -> Actions on Save
        - Check "Reformat code"
            - All file types
            - Whole file
                - Why not "Changed lines": The whole project should be formatted in this way. This
                  ensures that.
        - Optimize imports
            - Removes unused imports
            - Ensures styleguides on imports
    - [Source](https://www.jetbrains.com/help/idea/reformat-and-rearrange-code.html#reformat-on-save)

### Git

We follow the [conventional commit guidelines](https://www.conventionalcommits.org).