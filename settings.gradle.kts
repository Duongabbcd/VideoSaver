pluginManagement {
    repositories {
        google ()
        maven ("https://jitpack.io" )
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven ("https://jitpack.io" )

    }
}

rootProject.name = "VideoSaver"
include(":app")
