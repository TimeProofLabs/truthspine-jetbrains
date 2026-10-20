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
              <li>Offers TruthSpine on Linux: Get TruthSpine opens the AppImage download on truthspine.app (sold directly by TimeProofLabs), and the plugin connects once the AppImage has run on this computer.</li>
              <li>Scopes approval to the displayed folder and requests approval again when that scope changes.</li>
              <li>Uses the IDE project path directly and never searches the whole computer automatically.</li>
              <li>Names the package TruthSpine Connector and keeps setup visible until the protected desktop app is detected.</li>
              <li>Checks queued acquisition-resume state and distinguishes an opening request from a completed connection.</li>
              <li>Preserves the free connector boundary: trial, purchase, restore, and licensing remain owned by the protected desktop app and its app store.</li>
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
