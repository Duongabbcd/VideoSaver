plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
}

// Chaquopy must be applied *outside* the plugins {} block in Kotlin DSL
//apply(plugin = "com.chaquo.python")

android {
    namespace = "com.example.videosaver"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.videosaver"
        minSdk = 28
        targetSdk = 35
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
    }


}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Pull to Refresh
    implementation(libs.legacy.support)

    // For storing objects in shared preferences
    implementation(libs.gson)

    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.auth0:java-jwt:4.4.0")

    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-compiler:2.55")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")  // For Gson converter
    implementation("com.squareup.okhttp3:okhttp:4.9.0") // OkHttp for networking
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.8.6")



    //lottie
    implementation("com.airbnb.android:lottie:6.6.6")

    //glide
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.github.bumptech.glide:glide:4.15.1") // or your Glide version
    ksp("com.github.bumptech.glide:compiler:4.15.1")        // if using kapt


    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
    // HLS support
    implementation("androidx.media3:media3-exoplayer-hls:1.8.0")
    implementation("androidx.media3:media3-exoplayer-rtsp:1.8.0")

    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")
    implementation("androidx.media3:media3-extractor:1.8.0")
    implementation("androidx.media3:media3-database:1.8.0")
    implementation("androidx.media3:media3-decoder:1.8.0")
    implementation("androidx.media3:media3-datasource:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")

    implementation("androidx.media3:media3-datasource-okhttp:1.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    //scrapping video
    implementation("org.jsoup:jsoup:1.16.1")  // Or latest version

    val youtubedlAndroid = "0.5.1"
//    implementation("com.github.yausername:youtubedl-android:0.5.1")
    implementation("com.github.maxrave-dev:kotlin-youtubeExtractor:0.0.7")

    implementation("com.github.microshow:RxFFmpeg:4.9.0-lite")

    implementation("io.github.junkfood02.youtubedl-android:library:0.17.4")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.17.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    //room
    implementation("androidx.room:room-runtime:2.8.1")
    ksp("androidx.room:room-compiler:2.8.1")
    implementation("androidx.room:room-ktx:2.3.0")

    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava3:2.10.5")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:2.8.1")
    implementation ("com.squareup.retrofit2:adapter-rxjava3:2.11.0")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    // Because RxAndroid releases are few and far between, it is recommended you also
    // explicitly depend on RxJava's latest version for bug fixes and new features.
    // (see https://github.com/ReactiveX/RxJava/releases for latest 3.x.x version)
    implementation("io.reactivex.rxjava3:rxjava:3.1.10")

    implementation("androidx.webkit:webkit:1.12.1")
}