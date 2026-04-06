pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useVersion("4.2.2")
            }
            if (requested.id.id == "org.jetbrains.kotlin.android") {
                useVersion("1.8.22") // 🔥 ATUALIZADO
            }
        }
    }
}

rootProject.name = "CAINE_AI_TURBO_1.0"
include(":app")
