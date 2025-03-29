plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "nikmax.gallery.gallery.core"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 30
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
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
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Coil
    implementation(libs.coil.kt.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.coil.gif)
    // Icons
    implementation(libs.material.icons.extended)
    // Timber
    implementation(libs.timber)
    // Tika
    implementation(libs.tika.core)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    // Datastore
    implementation(libs.androidx.datastore.preferences)
    // Worker
    implementation(libs.androidx.work.runtime.ktx)
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.com.google.dagger.hilt.android.gradle.plugin)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)
}
