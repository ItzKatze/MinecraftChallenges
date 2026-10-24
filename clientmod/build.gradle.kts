plugins {
    id("net.fabricmc.fabric-loom")
    id("com.diffplug.spotless")
}

val paperVersion = providers.gradleProperty("paperVersion").get()
val minecraftVersion = paperVersion.substringBefore(".build")

version = "${project.version}+$minecraftVersion"

base {
    archivesName.set("KatzeGames-Client")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("fabricLoaderVersion").get()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabricApiVersion").get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    val props = mapOf("version" to version, "minecraft_version" to minecraftVersion)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

spotless {
    java {
        target("src/**/*.java")
        removeUnusedImports()
        toggleOffOn()
        trimTrailingWhitespace()
        formatAnnotations()
    }
}
