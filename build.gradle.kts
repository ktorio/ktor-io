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
    kotlin("multiplatform") version "1.6.20"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

allprojects {
    group = "io.ktor.io"
    apply(plugin="maven-publish")

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
            withJava()
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
        js(BOTH) {
            browser {
                commonWebpackConfig {
                    cssSupport.enabled = true
                }
            }
        }
        val hostOs = System.getProperty("os.name")
        val isMingwX64 = hostOs.startsWith("Windows")
        val nativeTarget = when {
            hostOs == "Mac OS X" -> macosX64("native")
            hostOs == "Linux" -> linuxX64("native")
            isMingwX64 -> mingwX64("native")
            else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
        }

        explicitApi()

        sourceSets {
            val commonMain by getting {
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            val jvmMain by getting
            val jvmTest by getting
            val jsMain by getting
            val jsTest by getting
            val nativeMain by getting
            val nativeTest by getting
        }
    }

    the<PublishingExtension>().apply {
        publications {
            create<MavenPublication>(project.name) {
                from(components["kotlin"])
            }
        }
    }
}

