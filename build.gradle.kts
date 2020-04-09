plugins {
    kotlin("jvm").version("1.3.71")
    id("com.apollographql.apollo").version("1.4.1")
}

repositories {
    jcenter()
    mavenCentral()
}

apollo {
    generateKotlinModels.set(true)
}