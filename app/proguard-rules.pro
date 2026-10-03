# Add project specific ProGuard rules here.
# Keep the launcher entry points and Compose runtime.

# Room / Hilt generated code is handled by their consumer rules.
-keep class androidx.room.** { *; }
-keep class dagger.hilt.** { *; }

# Keep composition-related metadata for Compose.
-dontwarn androidx.compose.**
