plugins {
    id("application")
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.lavalink.dev/releases") }
}

dependencies {
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("project.kristiyan.App")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "project.kristiyan.App"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
