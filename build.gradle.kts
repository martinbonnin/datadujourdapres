plugins {
    kotlin("jvm").version("1.3.71")
    kotlin("plugin.serialization").version("1.3.71")
    id("com.apollographql.apollo").version("1.4.1")
}

repositories {
    jcenter()
    mavenCentral()
}

buildscript {
    repositories {
        jcenter()
    }
}

dependencies {
    implementation("junit:junit:4.13")
    implementation("com.apollographql.apollo:apollo-runtime:1.4.1")
    implementation("com.apollographql.apollo:apollo-coroutines-support:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("com.opencsv:opencsv:5.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.71") // or "kotlin-stdlib-jdk8"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0") // JVM dependency
}

apollo {
    generateKotlinModels.set(true)
}