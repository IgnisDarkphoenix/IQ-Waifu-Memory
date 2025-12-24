# libGDX
-keep class com.badlogic.gdx.** { *; }
-keep class com.badlogic.gdx.backends.android.** { *; }

# Tu juego
-keep class com.waifu.memory.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Mantener nombres para debug
-keepattributes SourceFile,LineNumberTable

# Evitar warnings
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*