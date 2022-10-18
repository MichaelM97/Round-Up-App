import Dependencies.compose
import Dependencies.hilt

plugins {
    id("com.android.application")
    id(Plugins.KOTLIN_ANDROID)
    id(Plugins.KOTLIN_KAPT)
    id(Plugins.HILT)
}

android {
    namespace = "com.michaelmccormick.roundupapp"
    compileSdk = Versions.TARGET_SDK
    defaultConfig {
        applicationId = "com.michaelmccormick.roundupapp"
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.michaelmccormick.roundupapp.HiltTestRunner"
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }
    kotlinOptions.jvmTarget = Versions.JAVA.toString()
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.COMPOSE_COMPILER
    packagingOptions.resources.excludes.addAll(Configuration.EXCLUDED_PACKAGING_OPTIONS)
}

kapt.correctErrorTypes = true

dependencies {
    implementation(project(Modules.DATA))
    implementation(project(Modules.NETWORK))
    implementation(project(Modules.Features.RoundUp.UI))
    implementation(project(Modules.Features.RoundUp.DOMAIN))
    implementation(project(Modules.Core.MODELS))
    implementation(project(Modules.Core.FACTORIES))

    hilt()
    compose()
    implementation(Dependencies.AndroidX.ACTIVITY_COMPOSE)
    implementation(Dependencies.Logging.TIMBER)

    androidTestImplementation(Dependencies.UiTest.TEST_RUNNER)
    androidTestImplementation(Dependencies.UiTest.COMPOSE_UI_TEST)
    androidTestImplementation(Dependencies.UiTest.HILT_TEST)
    kaptAndroidTest(Dependencies.DI.HILT_COMPILER)
}
