plugins {
    id("java")
    id("maven-publish")
}

description = "AsyncVault Sponge Implementation"

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":asyncvault-api"))
    
    // Sponge API
    compileOnly("org.spongepowered:spongeapi:10.0.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    archiveBaseName.set("asyncvault-sponge")
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
