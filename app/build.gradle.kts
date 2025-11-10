plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize") // <-- add this here manually
}



android {
    namespace = "com.collage.new_strishakti"
    compileSdk = 36
    buildFeatures {
        viewBinding =true
    }

    defaultConfig {
        applicationId = "com.collage.new_strishakti"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.mediation.test.suite)
    implementation(libs.interactivemedia)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.leanback.paging)
    implementation(libs.filament.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("com.google.android.material:material:1.12.0")
    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle ViewModel & LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Retrofit for Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp for logging
    implementation(libs.logging.interceptor)

    // Material Components
    implementation(libs.material.v190)

    // Glide setup
    implementation("com.github.bumptech.glide:glide:5.0.5")

    // CircleImageView (for profile image)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ExoPlayer for video playback
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.datastore:datastore-preferences:1.1.7")
}
