rootProject.name = "AsyncVault"

include(
    "asyncvault-api",
    "asyncvault-spigot",
    "asyncvault-fabric",
    "asyncvault-sponge"
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
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io")
    }
}
