plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "tw.com.jasper.gh"
    compileSdk = 33

    defaultConfig {
        applicationId = "tw.com.jasper.gh"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // buildConfigField 'String', 'var01', '"var01_value_release"'
            // buildConfigField 'int', 'var02', '1'
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // buildConfigField 'String', 'var01', '"var01_value_debug"'
            // buildConfigField 'int', 'val02', '2'
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // log
    implementation("com.jakewharton.timber:timber:4.7.1")
    // @ColorRes @DrawableRes @StringRes
    implementation("androidx.annotation:annotation:1.7.0")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.1.0")
    implementation("com.squareup.retrofit2:converter-gson:2.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    // test framework
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    /*
        CameraX
     */
    val camerax_version = "1.4.0-alpha04"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    // If you want to additionally use the CameraX VideoCapture library
    // implementation "androidx.camera:camera-video:${camerax_version}"
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:${camerax_version}")
    // If you want to additionally add CameraX ML Kit Vision Integration
    // implementation "androidx.camera:camera-mlkit-vision:${camerax_version}"
    // If you want to additionally use the CameraX Extensions library
    // implementation "androidx.camera:camera-extensions:${camerax_version}"
}