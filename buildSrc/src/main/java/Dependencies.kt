object Apps {
    const val applicationId = "com.neobns.lifecyclejs"
    const val compileSdk = 30
    const val buildTools = "30.0.3"
    const val minSdk = 21
    const val targetSdk = 30
    const val versionCode = 1
    const val versionName = "1.0"
}

object Versions {
    const val gradle = "4.2.2"
    const val kotlin = "1.5.21"
    const val hilt = "2.32-alpha"
    const val appcompat = "1.3.0"
    const val coreKtx = "1.6.0"
    const val material = "1.4.0"
    const val constraintLayout = "2.0.4"
    const val base = "0.1"
    const val coroutine = "1.3.9"
    const val activityKtx = "1.2.3"
    const val gson = "2.8.7"

    const val junit = "4.13.2"
}

object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltCompiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
    const val base = "com.github.seoyoon630:Base:${Versions.base}"
    const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutine}"
    const val activityKtx = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
}

object TestLibs {
    const val junit = "junit:junit:${Versions.junit}"
}