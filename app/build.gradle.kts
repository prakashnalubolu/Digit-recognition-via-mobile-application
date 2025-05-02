plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.hci_project3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hci_project3"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
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

    packagingOptions {
        // Remove specific pickFirsts to avoid excluding necessary native libraries
        jniLibs.useLegacyPackaging = true
        jniLibs.keepDebugSymbols += listOf("**/*.so")
        // Only include common libraries if necessary
        pickFirsts.addAll(listOf("**/libc++_shared.so"))
    }

    // It's recommended to disable ABI splits initially to ensure all necessary .so files are included
    // You can enable and configure ABI splits later once everything works correctly
    /*
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
    */
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("net.sf.supercsv:super-csv:2.4.0")

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Use ONLY standard PyTorch Android dependencies
    implementation("org.pytorch:pytorch_android:1.13.1")
    implementation("org.pytorch:pytorch_android_torchvision:1.13.1")

    // If not needed, you can remove or comment this out:
    // implementation("org.pytorch:pytorch_android_lite:1.13.1")

    // For SoLoader (if needed by PyTorch, else you can remove)
    implementation("com.facebook.soloader:soloader:0.10.5")
}

