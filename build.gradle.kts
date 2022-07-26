/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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
    kotlin("multiplatform") version "1.6.20"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    `maven-publish`
}

group = "io.ktor"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

apply(plugin = "kotlin-multiplatform")
apply(plugin = "kotlinx-atomicfu")

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

    val platforms: List<KotlinNativeTarget> = listOf(
        mingwX64(),
        linuxX64(),
        macosX64(),
        macosArm64(),
        iosX64(),
        iosArm64(),
        iosArm32(),
        iosSimulatorArm64(),
        watchosX86(),
        watchosX64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
        tvosX64(),
        tvosArm64(),
        tvosSimulatorArm64()
    )

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

        val nativeMain by creating
        val nativeTest by creating

        nativeMain.dependsOn(commonMain)
        nativeTest.dependsOn(commonTest)
        nativeTest.dependsOn(nativeMain)

        val platformMain = platforms.map { sourceSets.getByName("${it.name}Main") }
        val platformTest = platforms.map { sourceSets.getByName("${it.name}Test") }

        platformMain.forEach {
            it.dependsOn(nativeMain)
        }

        platformTest.forEach {
            it.dependsOn(nativeTest)
        }
    }
}
