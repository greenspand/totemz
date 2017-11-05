# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\mihai\android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#For wire protocol buffers
-keep class com.squareup.wire.** { *; }
-keep class com.yourcompany.yourgeneratedcode.** { *; }

#Kodein
-keepattributes Signature

#paho rules
-keepclasseswithmembers class org.eclipse.paho.** { *; }
-keepclasseswithmembernames class org.eclipse.paho.** { *; }
-keep class org.eclipse.paho.** { *; }

#kotlin toolbelt
-keepclasseswithmembers class com.moovel.kotlintoolbelt.** { *; }
-keepclasseswithmembernames class com.moovel.kotlintoolbelt.** { *; }
-keep class com.moovel.kotlintoolbelt.** { *; }

#kotlin
-keepclassmembers class **$WhenMappings {
    <fields>;
}

#Moshi
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

#Koltin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# Retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

