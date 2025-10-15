plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ambrxsh.buzzbuddy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ambrxsh.buzzbuddy"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true

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
}

dependencies {
    val room_version = "2.8.0"
    val lifecycle_version = "2.9.3"

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")


    implementation("com.google.code.gson:gson:2.11.0")

    implementation("com.google.android.material:material:1.10.0")




    implementation("androidx.room:room-runtime:$room_version")

    ksp("androidx.room:room-compiler:$room_version")

    implementation("androidx.room:room-ktx:${room_version}")


    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${lifecycle_version}")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}