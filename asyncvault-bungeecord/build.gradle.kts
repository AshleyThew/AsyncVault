plugins {
    id("java")
    id("maven-publish")
}

description = "AsyncVault BungeeCord/Waterfall Implementation"

dependencies {
    implementation(project(":asyncvault-api"))
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("bungee.yml") {
        expand(props)
    }
}

tasks.jar {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    archiveBaseName.set("asyncvault-bungeecord")
    archiveVersion.set(project.version.toString())

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("asyncvault-api") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Implementation-Title" to project.name,
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
                classifier = "asyncvault-bungeecord"
            }
        }
    }
}
