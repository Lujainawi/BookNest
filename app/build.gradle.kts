import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

fun getApiKey(): String {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    return if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
        localProperties.getProperty("API_KEY", "")
    } else {
        ""
    }
}

android {
    namespace = "com.lujsom.booknest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lujsom.booknest"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"" + getApiKey() + "\"")
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    //A library that allows you to easily pull information from an API.
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    //Converts the information from JSON to objects in Java.
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

