import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.0"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = project.property("pluginGroup").toString()
version = project.property("pluginVersion").toString()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

// Configure IntelliJ Platform Plugin
intellij {
    pluginName.set(project.property("pluginName").toString())
    version.set(project.property("platformVersion").toString())
    type.set(project.property("platformType").toString())
    updateSinceUntilBuild.set(true)
    plugins.set(listOf("java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set(project.property("pluginSinceBuild").toString())

        // Extract the <!-- Plugin description --> section from Description.md
        pluginDescription.set(File(projectDir, "Description.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in Description.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
        })

        // Get the latest available change notes from the changelog file
        changeNotes.set(File(projectDir, "CHANGELOG.md").readText().let { changelog ->
            changelog.lines()
                .dropWhile { !it.startsWith("## [") }
                .takeWhile { !it.startsWith("## [") || it.startsWith("## [Unreleased]") }
                .joinToString("\n")
                .let(::markdownToHTML)
        })
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf(project.property("pluginVersion").toString()
            .split('-')
            .getOrElse(1) { "default" }
            .split('.')
            .first()))
    }
}

// Disable configuration cache as it's not fully supported yet
tasks.named("patchPluginXml") {
    notCompatibleWithConfigurationCache("Plugin XML patching requires Project instance")
}

// Configure Gradle Changelog Plugin
changelog {
    version.set(project.property("pluginVersion").toString())
    groups.set(emptyList())
}