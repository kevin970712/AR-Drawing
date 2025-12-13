# --- Android 基本規則 ---
-keep class **.R$* { *; }
-keep class **.R { *; }

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# --- Coil (圖片加載庫) ---
-keep class coil.** { *; }
-keepnames class coil.** { *; }
-keep class com.example.ardrawing.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Lifecycle & ViewModel ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Coroutines (協程) ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

# --- 其他 ---
-keep @androidx.annotation.Keep class * {*;}