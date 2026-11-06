import java.util.Properties

// Kredensial OAuth Drive dibaca dari local.properties (tidak pernah masuk repo).
// Isi drive.clientId + drive.clientSecret di sana; lihat local.properties.example.
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localProp(name: String): String =
    (localProperties.getProperty(name) ?: "").replace("\\", "\\\\").replace("\"", "\\\"")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "id.frmdeveloper.gallery"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.frmdeveloper.gallery"
        minSdk = 30
        targetSdk = 36
        versionCode = 52
        versionName = "1.51"
        buildConfigField("String", "DRIVE_CLIENT_ID", "\"${localProp("drive.clientId")}\"")
        buildConfigField("String", "DRIVE_CLIENT_SECRET", "\"${localProp("drive.clientSecret")}\"")
        buildConfigField("String", "DRIVE_WEB_CLIENT_ID", "\"${localProp(\"drive.webClientId\")}\"")
        buildConfigField("String", "DRIVE_WEB_CLIENT_SECRET", "\"${localProp(\"drive.webClientSecret\")}\"")
    }

    // TIDAK ADA kunci tanda tangan di repo ini. Build debug pakai kunci debug
    // bawaan Android Studio; buat rilis, tambahkan signingConfig sendiri
    // (keystore + password lewat local.properties) — jangan commit keystore.
    buildTypes {
        debug {
        }
        release {
            // v1.6: RILIS beneran (R8 + shrink) — build DEBUG yg dikirim selama
            // ini jauh lebih lambat (Compose tanpa optimasi), tersangka utama
            // lag yg gak sembuh-sembuh dari v1.3-v1.5.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    lint {
        // lint-gradle tidak ada di cache offline container — matikan cek lint
        // buat build rilis (lint bukan gerbang kualitas di project ini).
        checkReleaseBuilds = false
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("io.coil-kt:coil-video:2.7.0")
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    // Penulis tag audio (ID3/APIC dll) buat fitur ganti cover lagu — JAR
    // diunduh manual via curl (egress nge-reset Gradle), taruh di app/libs.
    implementation(files("libs/jaudiotagger.jar"))
    debugImplementation("androidx.compose.ui:ui-tooling")
}
