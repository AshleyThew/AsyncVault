rootProject.name = "AsyncVault"

include(
    "asyncvault-api",
    "asyncvault-spigot",
    "asyncvault-fabric",
    "asyncvault-sponge",
    "asyncvault-velocity",
    "asyncvault-bungeecord"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io")
    }
}
