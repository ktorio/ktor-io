plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(buildLibs.build.kotlin)
    implementation(buildLibs.build.kotlinx.atomicfu)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
