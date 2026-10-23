plugins {
    base
    id("java-library")
    id("io.freefair.lombok") version "9.1.0"
    id("com.diffplug.spotless") version "8.2.1"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val paperVersion = providers.gradleProperty("paperVersion").get()

allprojects {
    group = "gg.itzkatze"
    version = findProperty("releaseVersion") ?: "0.0.1-SNAPSHOT"
}

configure(subprojects.filter { it.name != "clientmod" }) {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        implementation("org.reflections:reflections:0.10.2")
        implementation("com.google.code.gson:gson:2.13.2")

        compileOnly("io.papermc.paper:paper-api:$paperVersion")

        compileOnly("org.projectlombok:lombok:1.18.42")
        compileOnly("org.jetbrains:annotations:26.0.1")
    }

    spotless {
        java {
            target("**/*.java")
            removeUnusedImports()
            toggleOffOn()
            trimTrailingWhitespace()
            formatAnnotations()
        }
    }
}
