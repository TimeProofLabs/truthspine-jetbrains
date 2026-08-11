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
            <ul>
              <li>Connects the exact project open in a JetBrains IDE to the protected TruthSpine app.</li>
              <li>Uses clearer customer-facing setup and license information.</li>
              <li>Adds a direct, optional JetBrains Marketplace review action.</li>
            </ul>
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
