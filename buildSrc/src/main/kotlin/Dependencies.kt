import org.gradle.kotlin.dsl.DependencyHandlerScope

object Dependencies {
    object AndroidX {
        const val COMPOSE_UI = "androidx.compose.ui:ui:${Versions.COMPOSE}"
        const val COMPOSE_MATERIAL = "androidx.compose.material:material:${Versions.COMPOSE}"
        const val LIFECYCLE_VIEWMODEL_COMPOSE = "androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1"
        const val ACTIVITY_COMPOSE = "androidx.activity:activity-compose:1.6.0"
    }

    object DI {
        const val HILT = "com.google.dagger:hilt-android:${Versions.HILT}"
        const val HILT_COMPILER = "com.google.dagger:hilt-compiler:${Versions.HILT}"
    }

    object Network {
        const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
        const val RETROFIT_MOSHI_CONVERTER = "com.squareup.retrofit2:converter-moshi:${Versions.RETROFIT}"
        const val MOSHI = "com.squareup.moshi:moshi:${Versions.MOSHI}"
        const val MOSHI_KOTLIN_CODEGEN = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.MOSHI}"
    }

    object Logging {
        const val TIMBER = "com.jakewharton.timber:timber:5.0.1"
    }

    object Test {
        const val JUNIT_5_BOM = "org.junit:junit-bom:5.9.1"
        const val JUNIT_5_JUPITER = "org.junit.jupiter:junit-jupiter"
        const val JUNIT_5_PARAMS = "org.junit.jupiter:junit-jupiter-params"
        const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.KOTLIN}"
        const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4"
        const val MOCKK = "io.mockk:mockk:1.13.1"
        const val TURBINE = "app.cash.turbine:turbine:0.11.0"
    }

    object UiTest {
        const val TEST_RUNNER = "androidx.test:runner:1.4.0"
        const val HILT_TEST = "com.google.dagger:hilt-android-testing:${Versions.HILT}"
        const val COMPOSE_UI_TEST = "androidx.compose.ui:ui-test-junit4:${Versions.COMPOSE}"
        const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest:${Versions.COMPOSE}"
    }

    fun DependencyHandlerScope.hilt() {
        "implementation"(DI.HILT)
        "kapt"(DI.HILT_COMPILER)
    }

    fun DependencyHandlerScope.compose() {
        "implementation"(AndroidX.COMPOSE_UI)
        "implementation"(AndroidX.COMPOSE_MATERIAL)
        "implementation"(AndroidX.LIFECYCLE_VIEWMODEL_COMPOSE)
    }
}
