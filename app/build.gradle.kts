import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"
    defaultConfig {
        applicationId = "com.yuk.miuihome"
        minSdk = 28
        targetSdk = 32
        versionCode = 4190
        versionName = "4.1.9"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro", "proguard-log.pro"))
        }
        create("noResHook") {
            initWith(getByName("release"))
        }
        sourceSets.getByName("main") {
            java.setSrcDirs(java.srcDirs + File(projectDir, "src/noResHook/java"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.bin"
            excludes += "/okhttp3/**"
        }
        dex {
            useLegacyPackaging = true
        }
        applicationVariants.all {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "MiuiHome-$versionName($versionCode)-$name.apk"
            }
        }
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.microsoft.appcenter:appcenter-crashes:4.4.2")
    implementation("com.microsoft.appcenter:appcenter-analytics:4.4.2")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
}
