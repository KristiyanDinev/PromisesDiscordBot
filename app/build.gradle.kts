
/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.13/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://maven.lavalink.dev/releases")
    }
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.guava)
    implementation(libs.jsckson)
    implementation(libs.jda)
    implementation(libs.sqlite)
    implementation(libs.slf4j)
    implementation(libs.logback)
    implementation(libs.lavaplayer)
    implementation(libs.jpa)
    implementation(libs.hibernate)
    implementation(libs.hibernateCommunityDialects)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "project.kristiyan.App"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "project.kristiyan.App"
    }
}