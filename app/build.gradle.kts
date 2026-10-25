plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rithika.lock"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rithika.lock"
        minSdk = 24
        targetSdk = 37
        versionCode = 5
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFileProp = project.findProperty("MYAPP_RELEASE_STORE_FILE") as String?
            if (storeFileProp != null) {
                storeFile = file(storeFileProp)
                storePassword = project.findProperty("MYAPP_RELEASE_STORE_PASSWORD") as String?
                keyAlias = project.findProperty("MYAPP_RELEASE_KEY_ALIAS") as String?
                keyPassword = project.findProperty("MYAPP_RELEASE_KEY_PASSWORD") as String?
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.okhttp)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val secretsFile = layout.projectDirectory.file("src/main/java/com/rithika/lock/data/api/Secrets.kt").asFile
val exampleFile = layout.projectDirectory.file("src/main/java/com/rithika/lock/data/api/Secrets.kt.example").asFile
if (!secretsFile.exists() && exampleFile.exists()) {
    exampleFile.copyTo(secretsFile)
}