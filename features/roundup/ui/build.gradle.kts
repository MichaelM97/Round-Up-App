import Dependencies.compose
import Dependencies.hilt

plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    id(Plugins.KOTLIN_KAPT)
    id(Plugins.HILT)
}

android {
    namespace = "com.michaelmccormick.features.roundup.ui"
    compileSdk = Versions.TARGET_SDK
    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }
    kotlinOptions.jvmTarget = Versions.JAVA.toString()
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.COMPOSE_COMPILER
    packagingOptions.resources.excludes.addAll(Configuration.EXCLUDED_PACKAGING_OPTIONS)
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

kapt.correctErrorTypes = true

dependencies {
    implementation(project(Modules.Features.RoundUp.DOMAIN))
    implementation(project(Modules.Core.UI))
    implementation(project(Modules.Core.MODELS))
    implementation(project(Modules.Core.FACTORIES))

    hilt()
    compose()

    testImplementation(project(Modules.Core.TEST))
    testImplementation(Dependencies.Test.TURBINE)

    androidTestImplementation(Dependencies.UiTest.COMPOSE_UI_TEST)
    androidTestImplementation(Dependencies.UiTest.COMPOSE_UI_TEST_MANIFEST)
    androidTestImplementation("io.mockk:mockk-android:1.13.1")
    //"org.jetbrains.kotlin:kotlin-test-junit:${Versions.KOTLIN}"
}
