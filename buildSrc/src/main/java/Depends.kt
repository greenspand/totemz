object Depends {

    object BuildPlugins {
        const val androidPlugin = "com.android.tools.build:gradle:${Versions.androidGradlePluginVersion}"
        const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    }

    object Android {
        const val supportAnnotations = "androidx.annotation:annotation:${Versions.androidSupportVersion}"
        const val lifecycleViewModel =
            "androidx.lifecycle:lifecycle-viewmodel:${Versions.androidLifecycleViewModelVersion}"
        const val lifecycleViewModelExtensions =
            "androidx.lifecycle:lifecycle-extensions:${Versions.androidLifecycleViewModelExtVersion}"
        const val ktxCore = "androidx.core:core-ktx:${Versions.androidKtxCoreVersion}"
        const val ktxLifecycleViewModel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidKtxLifecycleViewModelVersion}"
        const val supportAppcompat = "androidx.appcompat:appcompat:${Versions.androidSupportVersion}"
        const val material = " com.google.android.material:material:${Versions.androidMaterialVersion}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
        const val navFragment = "android.arch.navigation:navigation-fragment:${Versions.androidNavVersion}"
        const val navUi = "android.arch.navigation:navigation-ui:${Versions.androidNavVersion}"
    }

    object Kotlin {
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
        const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    }

    object Firebase {
        const val firebaseAuth = "com.google.firebase:firebase-auth:${Versions.firebaseAuthVersion}"
    }

    object Network {
        const val retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.retrofit2Version}"
        const val retrofit2CoroutinesAdapter =
            "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${Versions.retrofit2CorutinesAdapter}"
    }

    object TestLibraries {
        const val jUnit = "junit:junit:${Versions.junitVersion}"
        const val jUnitRunner = "androidx.test:runner:${Versions.junitRunnerVersion}"
        const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCoreVersion}"
        const val mockk = "io.mockk:mockk:${Versions.mockkVersion}"
    }

}