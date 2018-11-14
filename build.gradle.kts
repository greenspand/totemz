
buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.3.10"
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath(Depends.BuildPlugins.androidPlugin)
        classpath(Depends.BuildPlugins.kotlinPlugin)
        classpath(kotlin("gradle-plugin", kotlin_version))
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        maven { url = uri("https://maven.google.com") }
    }
}
