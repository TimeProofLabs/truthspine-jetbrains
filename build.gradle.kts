plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.timeprooflabs.truthspine"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val localIdePath = providers.gradleProperty("localIdePath")
        if (localIdePath.isPresent) {
            local(file(localIdePath.get()))
        } else {
            intellijIdeaCommunity("2025.2.6.1")
        }
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }
        changeNotes = """
            <p>Initial release. Connect the exact project open in a JetBrains IDE to local TruthSpine, open the TruthSpine app, or copy the chat attach phrase.</p>
        """.trimIndent()
    }
    pluginVerification {
        ides {
            current()
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }
}
