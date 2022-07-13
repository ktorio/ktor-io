/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.konan.target.*

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }

    val kotlin_version: String by extra
    val atomicfu_version: String by extra

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfu_version")
    }
}

plugins {
    kotlin("multiplatform") version "1.7.10"
    id("kotlinx-atomicfu") version "0.18.2"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

group = "io.ktor"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

val coroutines_version: String by extra

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser()
        nodejs()
    }

    mingwX64()
    linuxX64()
    macosX64()
    macosArm64()

    explicitApi()

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting

        val nativeMain by creating
        val nativeTest by creating

        nativeMain.dependsOn(commonMain)
        nativeTest.dependsOn(commonTest)
        nativeTest.dependsOn(nativeMain)

        val mingwX64Main by getting
        val mingwX64Test by getting
        val linuxX64Main by getting
        val linuxX64Test by getting
        val macosX64Main by getting
        val macosX64Test by getting
        val macosArm64Main by getting
        val macosArm64Test by getting

        listOf(linuxX64Main, mingwX64Main, macosX64Main, macosArm64Main).forEach {
            it.dependsOn(nativeMain)
        }

        listOf(linuxX64Test, mingwX64Test, macosX64Test, macosArm64Test).forEach {
            it.dependsOn(nativeTest)
        }
    }
}
