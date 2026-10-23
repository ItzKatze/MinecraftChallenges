pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
    plugins {
        id("net.fabricmc.fabric-loom") version providers.gradleProperty("loomVersion").get()
    }
}

rootProject.name = "MinecraftChallenges"

include(":loader")
include(":core")
include(":forcebattle")
include(":allitems")
include(":deathswap")
include(":randomizer")
include(":itemrain")

// The client mod needs Minecraft itself, which Loom downloads and sets up, so it joins the build only when it is asked for.
// IntelliJ syncs include it too, so the IDE knows the module; IntelliJ sets idea.sync.active only for syncs, never for task runs.
val clientmodRequested = gradle.startParameter.taskNames.any { it.startsWith("clientmod") || it.startsWith(":clientmod") }
val intellijSync = providers.systemProperty("idea.sync.active").orNull == "true"
if (clientmodRequested || intellijSync || providers.gradleProperty("includeClientmod").orNull == "true") {
    include(":clientmod")
}
