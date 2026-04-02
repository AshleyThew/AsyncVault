plugins {
    id("java")
    id("maven-publish")
}

description = "AsyncVault Core API - Async-first service abstractions"

dependencies {
    // No runtime dependencies - pure API module
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
}

tasks.jar {
    archiveBaseName.set("asyncvault-api")
    archiveVersion.set(project.version.toString())

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
                classifier = "asyncvault-api"
            }
        }
    }
}