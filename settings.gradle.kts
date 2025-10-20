pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // maven { url = uri("https://jitpack.io") } // <-- QUITA ESTA LÍNEA DE AQUÍ
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RedesOSI"
include(":app")