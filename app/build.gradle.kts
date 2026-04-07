plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.collage.empowermentstrishakti"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.collage.empowermentstrishakti"
        minSdk = 26
        targetSdk = 36
        versionCode = 35





        versionName = "8.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Ship only ARM. This keeps the x86_64 Filament .so out of your bundle.
//        ndk {
//            abiFilters += listOf("arm64-v8a") // add "armeabi-v7a" only if you still support 32-bit
//        }
    }

    buildFeatures { viewBinding = true }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
            // ✅ Belt & suspenders: strip any leftover x86 libs from transitive deps.
            excludes += listOf("**/x86/**", "**/x86_64/**")
        }
    }
}

dependencies {
    // ❗ Use ONE coordinate; you currently pull Filament twice.
    // Option A: keep Sceneform Filament:
    api("com.google.ar.sceneform:filament-android:1.17.1")
    // and REMOVE this if it points to the same thing:
    // implementation(libs.filament.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.leanback.paging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle ViewModel & LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Retrofit / OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Material
    implementation("com.google.android.material:material:1.12.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:5.0.5")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ExoPlayer / Media3
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.play:app-update:2.1.0")
    // Ads (pick ONE)
   // implementation("com.google.android.gms:play-services-ads:23.+")
    // implementation("com.google.android.gms:play-services-ads-lite:23.+")

    // STOMP WebSocket
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

// RxJava required for StompClient
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

// Other dependencies
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.github.bumptech.glide:glide:4.14.2")



// optional for avatars
}
