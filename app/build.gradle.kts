plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Plugin Compose
    alias(libs.plugins.hilt)           // Hilt
    alias(libs.plugins.ksp)            // KSP para Room y Hilt
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.josprox.redesosi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.josprox.aprendemas"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Configuración para generar esquemas de Room
        //noinspection WrongGradleMethod
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "DOTENV_KEY", System.getenv("DOTENV_KEY") ?: "\"\"")
        }

        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "DOTENV_KEY", System.getenv("DOTENV_KEY") ?: "\"\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
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

    // Esto asegura que Compose use la versión correcta del compilador
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.ui.tooling)

    // --- Navegación ---
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Hilt (Inyección de dependencias) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // --- Room (Base de datos local) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Ktor (Cliente HTTP para Groq API) ---
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // --- Serialización JSON ---
    implementation(libs.kotlinx.serialization.json)

    // --- Renderizado de Markdown ---
    implementation(libs.markdown.renderer.core)
    implementation(libs.markdown.renderer.m3) // Para el estilo M3
    implementation(libs.markdown.renderer.code) // Para el Resaltado de Sintaxis


    // -- Dotenv Vault (Env) ---
    implementation(libs.dotenv.vault.kotlin)

    // --- Testing
    implementation(libs.kotlinx.serialization.json)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
