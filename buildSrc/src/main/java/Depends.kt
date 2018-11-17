object Depends {

    object BuildPlugins {
        const val androidPlugin = "com.android.tools.build:gradle:${Versions.androidGradlePluginVersion}"
        const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    }

    object Android {
        /*Android X*/
        const val supportAnnotations = "androidx.annotation:annotation:${Versions.androidSupportVersion}"
        const val lifecycleExt =
            "androidx.lifecycle:lifecycle-extensions-ktx:${Versions.lifecycleVersion}"
        const val lifecycleRuntime =
            "androidx.lifecycle:lifecycle-runtime:${Versions.lifecycleVersion}"
        const val ktxCore = "androidx.core:core-ktx:${Versions.androidKtxCoreVersion}"
        const val material = " com.google.android.material:material:${Versions.androidMaterialVersion}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
        const val supportAppCompat = "androidx.appcompat:appcompat:${Versions.androidSupportVersion}"

        /*Pre Android X*/
        const val lifecycleExtOld = "android.arch.lifecycle:extensions:${Versions.lifecycleVersionOld}"
        const val lifecycleRuntimeOld = "android.arch.lifecycle:runtime:${Versions.lifecycleVersionOld}"
        const val navFragment = "android.arch.navigation:navigation-fragment:${Versions.androidNavVersion}"
        const val navUi = "android.arch.navigation:navigation-ui:${Versions.androidNavVersion}"
        const val navFragmentTesting = "android.arch.navigation:navigation-testing:${Versions.androidNavVersion}"
    }

    object CI {
        const val hockeyApp = "net.hockeyapp.android:HockeySDK:5.1.1"
    }

    object Kotlin {
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
        const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    }

    object UserAuth {
        const val firebaseAuth = "com.google.firebase:firebase-auth:${Versions.firebaseAuthVersion}"
        const val fbLogin = "com.facebook.android:facebook-android-sdk:[4,5)"
        const val twitterLogin = "com.twitter.sdk.android:twitter:3.3.0@aar"
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