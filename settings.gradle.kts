/*
 * Configures the Gradle modules for the Compose notes app.
 * Connects to: app/build.gradle.kts and root build.gradle.kts.
 * Created: 2026-07-03
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "QuickNotes"
include(":app")
