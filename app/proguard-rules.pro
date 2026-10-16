# Proguard rules for Petrol Map UK

# Data Models
-keep class com.zeetech.petrolmap.fuelpricesuk.data.model.** { *; }

# Osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**

# Play Core (In-App Review & In-App Updates)
-keep class com.google.android.play.core.** { *; }
