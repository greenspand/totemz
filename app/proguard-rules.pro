#For wire protocol buffers
-keep class com.squareup.wire.** { *; }
-keep class com.yourcompany.yourgeneratedcode.** { *; }


#paho rules
-keepclasseswithmembers class org.eclipse.paho.** { *; }
-keepclasseswithmembernames class org.eclipse.paho.** { *; }
-keep class org.eclipse.paho.** { *; }

#Kodein
-keepattributes Signature

#kotlin toolbelt
-keepclasseswithmembers class com.greenspand.kotlin-ext.** { *; }
-keepclasseswithmembernames com.greenspand.kotlin-ext.** { *; }
-keep class com.greenspand.kotlin-ext.** { *; }

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

