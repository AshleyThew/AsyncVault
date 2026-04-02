plugins {
    id("java")
    id("maven-publish")
    id("fabric-loom") version "1.3.9"
}

description = "AsyncVault Fabric Implementation"

dependencies {
    implementation(project(":asyncvault-api"))
    
    // Fabric API
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.15.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1")
    
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

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.jar {
    archiveClassifier.set("dev")

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "AsyncVault"
        )
    }
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    archiveBaseName.set("asyncvault-fabric")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
}
