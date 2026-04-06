// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "4.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false // 🔥 atualizado
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// 🔥 FORÇA VERSÕES CONSISTENTES (RESOLVE CONFLITOS)
subprojects {
    configurations.all {

        resolutionStrategy {

            force(

                // ==========================
                // 🧠 KOTLIN (ATUALIZADO)
                // ==========================
                "org.jetbrains.kotlin:kotlin-stdlib:1.8.22",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.22",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22",
                "org.jetbrains.kotlin:kotlin-stdlib-common:1.8.22",

                // ==========================
                // ⚡ CORE
                // ==========================
                "androidx.core:core-ktx:1.6.0",
                "androidx.core:core:1.6.0",

                // ==========================
                // 🎨 UI BASE
                // ==========================
                "androidx.appcompat:appcompat:1.3.1",

                // ==========================
                // 🔥 ACTIVITY
                // ==========================
                "androidx.activity:activity:1.3.1",

                // ==========================
                // 🔥 LIFECYCLE
                // ==========================
                "androidx.lifecycle:lifecycle-runtime:2.3.1",
                "androidx.lifecycle:lifecycle-common:2.3.1",
                "androidx.lifecycle:lifecycle-viewmodel:2.3.1",
                "androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.1",
                "androidx.lifecycle:lifecycle-livedata:2.3.1",
                "androidx.lifecycle:lifecycle-livedata-core:2.3.1",

                // ==========================
                // 💾 SAVED STATE
                // ==========================
                "androidx.savedstate:savedstate:1.1.0",

                // ==========================
                // 📦 COLLECTION
                // ==========================
                "androidx.collection:collection:1.1.0",

                // ==========================
                // 🧱 ARCH CORE
                // ==========================
                "androidx.arch.core:core-runtime:2.1.0",

                // ==========================
                // 🏷️ ANNOTATION
                // ==========================
                "androidx.annotation:annotation:1.2.0",
                "androidx.annotation:annotation-experimental:1.1.0",

                // ==========================
                // 🔊 COROUTINES
                // ==========================
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2"
            )
        }
    }
}
