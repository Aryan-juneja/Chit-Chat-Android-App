plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp.android)
}

android {
    namespace = "com.example.whatsappclone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.whatsappclone"
        minSdk = 27
        targetSdk = 34
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
    buildFeatures {
        viewBinding= true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation ("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation ("com.vanniktech:emoji:0.16.0")
    implementation ("com.vanniktech:emoji-ios:0.16.0") // For iOS-style emojis, adjust version if necessary
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1@aar")
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation (libs.lottie)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.firebase.ui.database.v802)
    implementation(libs.firebase.database)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.messaging.ktx)
    val paging_version = "3.3.2"
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation (libs.flexbox)
    implementation (libs.firebase.ui.firestore)
    implementation("androidx.paging:paging-runtime:$paging_version")
    implementation (libs.glide)
    implementation (libs.circleimageview)
    implementation (libs.ccp)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}