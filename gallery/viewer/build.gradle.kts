plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nikmax.gallery.viewer"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 30
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":core"))
    implementation(project(":gallery:dialogs"))
    implementation(project(":gallery:core"))
    
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Coil
    implementation(libs.coil.kt.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.coil.gif)
    // Media3
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // Icons
    implementation(libs.material.icons.extended)
    // Seekbar component from 2307vivek
    implementation(libs.seeker)
    // hilt
    // Dagger Core
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    // Dagger Android
    api(libs.dagger.android)
    api(libs.dagger.android.support)
    ksp(libs.dagger.android.processor)
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.com.google.dagger.hilt.android.gradle.plugin)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)
    // Activity KTX for viewModels()
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.ktx)
    // Hilt testing
    testImplementation(libs.hilt.core)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android.testing)
    // Timber
    implementation(libs.timber)
    // Worker
    implementation(libs.androidx.work.runtime.ktx)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
}
