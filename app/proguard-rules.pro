# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/czbix/Data/Apps/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# rules
-keep class android.support.design.widget.* {
    public *;
}

-keep class android.support.v7.widget.* {
    public *;
}

# for stacktrace
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# for Crashlytics
-keepattributes *Annotation*
-keep class com.crashlytics.android.**

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson model
-keepclassmembernames class com.czbix.v2ex.model.** {
    !static <fields>;
}

# EventBus subscribe
-keepclassmembers class ** {
    @com.google.common.eventbus.Subscribe <methods>;
}

# jsoup
-keeppackagenames org.jsoup.nodes

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}


# unresolve class in library
-dontwarn com.google.**
-dontwarn okio.**
