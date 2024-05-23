# Contributing to the Project

Thank you for your interest in contributing to the TREND project!

## Table of contents

* [CLA](#cla)
* [Git(Hub) Process](#github-process)
    * [Issues](#issues)
    * [Forks & Branches](#forks--branches)
    * [Pull Requests](#pull-requests)
    * [Commits](#commits)
    * [License Header](#license-header)
* [IntelliJ](#intellij)
* [Style Guide](#style-guide)
    * [IntelliJ IDEA configuration](#intellij-idea-configuration)

## CLA

When contributing to this project, the _Corporate Contributor License Agreement (“CLA”)_ must be
accepted. See the `CLA.md` file for more information.

## Git(Hub) Process

Please carefully follow our guidelines for the process with Git and GitHub. Failure to comply 
may result in rejection of the contribution.

### Issues

To organize and structure all contributions, open an issue for enhancements, feature requests or
bugs. Please use the provided templates and fill out all possible sections. In case of security
issues, see `SECURITY.md` for more details.

### Forks & Branches

When working on an issue, create a branch in the repository (if you have write permission) or fork
the repository (if you do not have write permission). Create a branch with a descriptive name (like
`docs/update-readme`), commit your changes as described here and create a pull request afterward.

### Pull Requests

Code changes are handled entirely over pull requests. When proposing a change, create a pull
request from your working branch or fork to the upstream `main` branch, fill out the template, 
link it to at least one issue (mandatory) and accept the CLA by keeping the text untouched.

We use the
[squash and merge](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/about-pull-request-merges#squash-and-merge-your-commits)
strategy. Therefore, remember to use a pull request title in the
[conventional commits](https://www.conventionalcommits.org/) format since it is used as the squashed
commit message. Since this repository uses a
[monolithic repository approach](https://en.wikipedia.org/wiki/Monorepo), the conventional
commit _scope_ should be set to the affected component, resulting in the following format for
the pull request title:

```
<type>(<component>): <descriptive title>
```

An example for an added compression feature in the `watermarker` library would be:

```
feat(watermarker): add watermark compression option
```

As already mentioned, every pull request has to be related to at least one issue. This guarantees
that every proposed contribution is related to a previously discussed issue. Pull requests
shouldn't be too big to allow a fast integration of changes. We recommend keeping the content
below 1000 lines of code (LOC).

To ensure code quality, every pull request needs at least one approved review from one of the
Committer team. Further check that all pipelines succeed before a review can be started.

### Commits

Due to the _squash and merge_, the final squashed commit is based on the title of the pull
request with a link to the pull request. The above example results in:
```
feat(watermarker): add watermark compression option (#123)
```
Even if all commits are squashed, we highly recommend using conventional commits to reflect all 
changes. Tools like [commitlint](https://github.com/conventional-changelog/commitlint) are 
suggested and can help to stay with the format. Further, we highly recommend to sign every commit 
with a GPG signature to enable the _Verfied_ flag on [GitHub](https://docs.github.com/en/authentication/managing-commit-signature-verification/displaying-verification-statuses-for-all-of-your-commits).

### License Header

Every source code file in this project must contain a license header. Example:

```
/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
```

## IntelliJ

When using IntelliJ, the following describes on how to open and use the project. To work on all
subprojects from one IntelliJ instance, you may have to manually add the `build.gradle.kts` files.

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

Remember that every method must contain a comment since it is used in the automatic 
documentation generation process. Therefore, get familiar with the
[KDoc syntax](https://kotlinlang.org/docs/kotlin-doc.html#kdoc-syntax) (similar to Javadoc) and 
use it for code comments in classes and functions.

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
