/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

// Versions
val kvisionVersion = "7.4.5"

val webDir = file("src/jsMain/web")

plugins {
    val kotlinVersion = "1.9.23"
    val kvisionVersion = "7.4.5"

    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    id("io.kvision") version kvisionVersion
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}

version = "0.1.0-SNAPSHOT"
group = "de.fraunhofer.isst.trend"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig(
                Action {
                    outputFileName = "main.bundle.js"
                },
            )
            runTask(
                Action {
                    mainOutputFileName = "main.bundle.js"
                    sourceMaps = false
                    devServer =
                        KotlinWebpackConfig.DevServer(
                            open = false,
                            port = 3000,
                            proxy =
                                mutableMapOf(
                                    "/kv/*" to "http://localhost:8080",
                                    "/kvws/*" to
                                        mapOf("target" to "ws://localhost:8080", "ws" to true),
                                ),
                            static = mutableListOf("$buildDir/processedResources/js/main"),
                        )
                },
            )
            testTask(
                Action {
                    useKarma {
                        useChromeHeadless()
                    }
                },
            )
        }
        binaries.executable()
    }

    sourceSets["jsMain"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
        implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
        implementation("io.kvision:kvision-state:$kvisionVersion")
        implementation("io.kvision:kvision-state-flow:$kvisionVersion")
        implementation("de.fraunhofer.isst.trend:watermarker:0.1.0-SNAPSHOT")
    }
    sourceSets["jsTest"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
}

task("test") {
    dependsOn("jsTest")
}
