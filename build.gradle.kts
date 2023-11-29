plugins {
    kotlin("jvm") version "1.9.20"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:testcontainers:1.17.2")
}

repositories {
    mavenCentral()
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
