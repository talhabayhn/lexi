# Lexi release (R8) rules. Most libraries ship their own consumer rules (kotlinx.serialization, Koin,
# SQLDelight, Compose, Kermit); only what R8 cannot see is listed here. Extend from CI output.

# Readable crash stack traces (mapping.txt is uploaded as a CI artifact).
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,Signature,EnclosingMethod
-renamesourcefileattribute SourceFile

# kotlinx.serialization: type-safe Navigation routes and DTOs look up `Companion.serializer()` reflectively.
-keepclassmembers @kotlinx.serialization.Serializable class dev.talhabayhan.lexi.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class dev.talhabayhan.lexi.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# SQLDelight Android driver uses the framework SQLite API only; nothing to keep.
# Compose resources are read from assets by path; resource shrinking does not touch assets.
