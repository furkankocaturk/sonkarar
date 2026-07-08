# Firestore DTO'larının alan adları yansıma ile eşlendiği için korunur.
-keepclassmembers class com.sonkarar.data.firestore.dto.** {
    <fields>;
    <init>();
}

# kotlinx.serialization ile işaretli sınıflar korunur.
-keepclassmembers,allowshrinking,allowobfuscation class com.sonkarar.data.uzak.dto.** {
    *;
}
