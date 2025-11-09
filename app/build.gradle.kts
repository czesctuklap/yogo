plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.chaquo.python")
}

android {
    namespace = "com.example.yogoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.yogoapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }//pls work
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
    buildFeatures {
        viewBinding = true
    }
    flavorDimensions += "pyVersion"
    productFlavors {
        create("py310") { dimension = "pyVersion" }
    }
}

chaquopy {
    defaultConfig {
        version = "3.10"
        pip {
//            // NumPy 1.23.3 (zgodne z scipy 1.8.1)
//            install("https://chaquo.com/pypi-13.1/numpy/numpy-1.23.3-0-cp310-cp310-android_21_arm64_v8a.whl")
//            install("https://chaquo.com/pypi-13.1/numpy/numpy-1.23.3-0-cp310-cp310-android_21_x86.whl")
//            install("https://chaquo.com/pypi-13.1/numpy/numpy-1.23.3-0-cp310-cp310-android_21_x86_64.whl")
//
//            // SciPy 1.8.1
//            install("https://chaquo.com/pypi-13.1/scipy/scipy-1.8.1-1-cp310-cp310-android_21_arm64_v8a.whl")
//            install("https://chaquo.com/pypi-13.1/scipy/scipy-1.8.1-1-cp310-cp310-android_21_x86.whl")
//            install("https://chaquo.com/pypi-13.1/scipy/scipy-1.8.1-1-cp310-cp310-android_21_x86_64.whl")
//
//            // scikit-learn 1.3.2
//            install("https://chaquo.com/pypi-13.1/scikit-learn/scikit_learn-1.3.2-0-cp310-cp310-android_21_arm64_v8a.whl")
//            install("https://chaquo.com/pypi-13.1/scikit-learn/scikit_learn-1.3.2-0-cp310-cp310-android_21_x86.whl")
//            install("https://chaquo.com/pypi-13.1/scikit-learn/scikit_learn-1.3.2-0-cp310-cp310-android_21_x86_64.whl")
        }
    }
    sourceSets {
        getByName("main") {
            srcDir("app/src/main/python")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //implementation("androidx.activity:activity-ktx:1.10.1")
    implementation(libs.youtube.player.core)
    implementation(libs.youtube.player.custom.ui)
}