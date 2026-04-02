plugins {
    id("java")
    id("maven-publish")
}

description = "AsyncVault Spigot/Paper Implementation"

dependencies {
    implementation(project(":asyncvault-api"))
    
    // Paper API (includes Bukkit, Spigot)
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    archiveBaseName.set("asyncvault-spigot")
    archiveVersion.set(project.version.toString())

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("asyncvault-api") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Implementation-Title" to "AsyncVault-Spigot",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "AsyncVault"
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.AshleyThew"
            artifactId = "AsyncVault"
            version = project.version.toString()
            artifact(tasks.jar.get()) {
                classifier = "asyncvault-spigot"
            }
        }
    }
}
