/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
plugins {
    kotlin("multiplatform") version "1.9.23"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
}

group = "de.fraunhofer.isst.trend"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser { }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(npm("pako", "2.1.0"))
            }
        }
        val jsTest by getting
    }
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
