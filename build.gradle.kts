import org.gradle.kotlin.dsl.jar

plugins {
    id("encoding-defaults")
    id("kotlin")
    id("library")
    alias(libs.plugins.intellij.platform)
}

group = "com.eightackerbird"
version = "1.0.0"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        create("IU", libs.versions.intellij.idea.get())
    }
}

intellijPlatform {
    instrumentCode = false
    buildSearchableOptions = false

    pluginConfiguration {
        name.set("Monorepo Tree Structure Folding")

        ideaVersion {
            sinceBuild = "262"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks.test {
    useJUnit()
    failOnNoDiscoveredTests = false
}

tasks.build {
    dependsOn(
        tasks.named("verifyPluginProjectConfiguration"),
        tasks.named("buildPlugin"),
    )
}

tasks.jar {
    destinationDirectory.set(layout.buildDirectory.dir("tmp/baseJar"))
}

tasks.assemble {
    dependsOn(tasks.named("composedJar"))
}