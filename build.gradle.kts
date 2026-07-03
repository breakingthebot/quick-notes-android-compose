/*
 * Declares shared Gradle plugins for the Compose notes app.
 * Connects to: settings.gradle.kts and app/build.gradle.kts.
 * Created: 2026-07-03
 */
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
