/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    org.jetbrains.kotlin.multiplatform
    `kotlinx-atomicfu`
    `maven-publish`
}

atomicfu {
    dependenciesVersion = libs.versions.kotlinx.atomicfu.get()
}

kotlin {
    jvm()
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
                api(libs.kotlinx.coroutines.test)
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
