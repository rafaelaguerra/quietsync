# Consumer ProGuard rules for the :shared KMP library module.
# Merged automatically into the app release build.

-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Kotlinx Serialization models used for local persistence
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

-keepclassmembers enum com.rafaelaguerra.synctask.domain.model.PhoneState {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Compose Multiplatform generated resources
-keep class com.rafaelaguerra.synctask.resources.** { *; }
