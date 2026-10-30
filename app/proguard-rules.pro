# ==============================================================================
# QuizEarn — ProGuard / R8 Release Optimization & Obfuscation Rules
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Source Debugging & Crash Attribution
# ------------------------------------------------------------------------------
# Preserve line numbers and source file names in stack traces for crash diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------------------
# 2. QuizEarn Domain Models & Data Transfer Objects
# ------------------------------------------------------------------------------
# Preserve domain entities, serialized data classes, and enums
-keep class com.quizearn.app.domain.model.** { *; }
-keepclassmembers class com.quizearn.app.domain.model.** { *; }
-keepclassmembers enum com.quizearn.app.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve local session storage entities and state objects
-keep class com.quizearn.app.data.local.** { *; }
-keepclassmembers class com.quizearn.app.data.local.** { *; }

# Preserve remote data models and evaluation payloads
-keep class com.quizearn.app.data.remote.** { *; }
-keepclassmembers class com.quizearn.app.data.remote.** { *; }

# Preserve result sealed hierarchy and metadata
-keep class com.quizearn.app.core.result.** { *; }
-keepclassmembers class com.quizearn.app.core.result.** { *; }

# Preserve UI state wrappers and summary models
-keep class com.quizearn.app.ui.screens.quiz.QuizResultSummary { *; }
-keep class com.quizearn.app.ui.screens.quiz.EvaluatedQuizResult { *; }
-keep class com.quizearn.app.ui.screens.quiz.QuestionReviewItem { *; }
-keep class com.quizearn.app.ui.screens.quiz.PerformanceTier { *; }

# ------------------------------------------------------------------------------
# 3. Kotlinx Serialization
# ------------------------------------------------------------------------------
# Keep serializer companions for reflective instantiation
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    public static *** Companion;
}
-keepclasseswithmembers class * {
    public static *** Companion;
}
-keep class * implements kotlinx.serialization.KSerializer {
    <init>(...);
}

# ------------------------------------------------------------------------------
# 4. AndroidX DataStore & Preferences
# ------------------------------------------------------------------------------
-keep class androidx.datastore.preferences.core.** { *; }
-dontwarn androidx.datastore.**

# ------------------------------------------------------------------------------
# 5. Kotlin Coroutines & StateFlow
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# 6. Jetpack Compose
# ------------------------------------------------------------------------------
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ------------------------------------------------------------------------------
# 7. AndroidX ViewModel & Lifecycle
# ------------------------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# ------------------------------------------------------------------------------
# 8. Navigation & Screens
# ------------------------------------------------------------------------------
-keep class com.quizearn.app.navigation.** { *; }
-keepclassmembers class com.quizearn.app.navigation.** { *; }

