plugins {
    id("com.android.application") version "8.7.2" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    }
}
