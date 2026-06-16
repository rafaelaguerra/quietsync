# =============================================================================
# SyncTask / QuietSync — Release ProGuard & R8 rules
# =============================================================================

# --- Debugging stack traces (obfuscated builds) ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin ---
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- Kotlinx Serialization ---
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rafaelaguerra.synctask.domain.model.**$$serializer {
    *;
}
-keepclassmembers class com.rafaelaguerra.synctask.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontnote kotlinx.serialization.AnnotationsKt

# Enums referenced by name (PhoneStateReceiver uses PhoneState.valueOf)
-keepclassmembers enum com.rafaelaguerra.synctask.domain.model.PhoneState {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.compose.**

# --- Compose Multiplatform resources ---
-keep class com.rafaelaguerra.synctask.resources.** { *; }

# --- Android components declared in the manifest ---
-keep class com.rafaelaguerra.synctask.MainActivity { *; }
-keep class com.rafaelaguerra.synctask.SyncTaskApplication { *; }
-keep class com.rafaelaguerra.synctask.data.receiver.PhoneStateReceiver { *; }

# --- Google Play Billing ---
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }

# --- Firebase Remote Config ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.ktx.Firebase

# --- Play In-App Updates ---
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Lottie ---
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# --- Strip verbose logging in release (keep warnings/errors) ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# --- General Android ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * extends android.app.Application {
    <init>();
}
