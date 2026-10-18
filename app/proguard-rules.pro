# Proguard rules for Petrol Map UK

# Data Models
-keep class com.zeetech.petrolmap.fuelpricesuk.data.model.** { *; }

# Osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**

# Play Core (In-App Review & In-App Updates) & Play Services
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.gms.internal.location.**
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite

# Kotlin Standard Library & Coroutines
-keep class kotlin.** { *; }
-dontwarn kotlin.**
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Firebase Crashlytics
-keepattributes SourceFile, LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
