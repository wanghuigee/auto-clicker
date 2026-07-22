# ProGuard rules for Auto Clicker

# Keep Gson serialization classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.auto.clicker.model.** { *; }
-keep class com.google.gson.** { *; }

# Keep Accessibility Service
-keep class com.auto.clicker.service.** { *; }
