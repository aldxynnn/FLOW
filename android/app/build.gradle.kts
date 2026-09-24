plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.aldxynnn.flow"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aldxynnn.flow"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.1.0"
        val configuredApiBaseUrl = providers.gradleProperty("FLOW_API_BASE_URL")
            .orElse("http://10.0.2.2:8080/api/")
            .get()
            .trim()
        require(configuredApiBaseUrl.startsWith("http://") || configuredApiBaseUrl.startsWith("https://")) {
            "FLOW_API_BASE_URL must start with http:// or https://"
        }
        val apiBaseUrl = if (configuredApiBaseUrl.endsWith("/")) configuredApiBaseUrl else "$configuredApiBaseUrl/"
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.replace("\"", "\\\"")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")

    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("de.afarber:openmapview:0.13.3")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
