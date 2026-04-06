pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // 🔥 FORÇA VERSÃO COMPATÍVEL COM JAVA 8 (CORRIGIDO)
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useVersion("4.2.2") // ✅ compatível REAL com Java 8
            }
            if (requested.id.id == "org.jetbrains.kotlin.android") {
                useVersion("1.6.21") // ✅ alinhado com AGP 4.2.2
            }
        }
    }
}

rootProject.name = "CAINE_AI_TURBO_1.0"
include(":app")