pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Round Up App"
include(":app")
include(":network")
include(":data")
include(":core:factories")
include(":core:models")
include(":features:roundup:domain")
include(":features:roundup:ui")
include(":core:ui")
include(":core:test")
