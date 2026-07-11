# --- BorrowNest ProGuard / R8 rules ---

# Keep kotlinx.serialization generated serializers and metadata.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the serialization runtime.
-keep,includedescriptorclasses class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep @Serializable classes in this app and their generated companions/serializers.
-keepclassmembers @kotlinx.serialization.Serializable class com.borrownest.app.** {
    *** Companion;
    <fields>;
}
-keepclasseswithmembers class com.borrownest.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.borrownest.app.model.** { *; }

# Kotlin metadata.
-keep class kotlin.Metadata { *; }

# Compose does not require special rules with R8, but keep tooling-safe defaults.
-dontwarn org.jetbrains.annotations.**
