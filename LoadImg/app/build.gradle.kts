plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.bt09"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bt09"
        minSdk = 24
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)

    // Các thư viện mạng
    implementation(libs.retrofit)  // Thư viện Retrofit
    implementation(libs.converter.gson)  // Converter cho Gson
    implementation(libs.gson)  // Thư viện Gson

    // Thư viện tải ảnh
    implementation(libs.glide)  // Glide để tải hình ảnh

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}