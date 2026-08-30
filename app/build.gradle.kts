plugins {
    id("com.android.application")
}

android {
    namespace = "dev.zwz.pipedreamadblocker"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "dev.zwz.pipedreamadblocker"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "1.1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
        }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
}
