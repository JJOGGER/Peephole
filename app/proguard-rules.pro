# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}


#---------------------------------2.第三方包-------------------------------
#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
-dontwarn com.fbee.ir.etclass.**
-keep class com.fbee.ir.etclass.**{*;}
-dontwarn com.fbee.ir.etutil.**
-keep class com.fbee.ir.etutil.**{*;}
-dontwarn com.hp.hpl.sparta.**
-keep class com.hp.hpl.sparta.**{*;}
-dontwarn net.sourceforge.pinyin4j.**
-keep class net.sourceforge.pinyin4j.**{*;}
-dontwarn pinyindb.**
-keep class pinyindb.**{*;}
-dontwarn demo.**
-keep class demo.*
#-keep me.iwf.photopicker** { *; }
#-keep com.mcxtzhang.layoutmanager.** { *; }
#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#okhttp3.x
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

## nineoldandroids-2.4.0.jar
-keep public class com.nineoldandroids.** {*;}
####################zxing#####################
-keep class com.google.zxing.** {*;}
-dontwarn com.google.zxing.**
## okhttp
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.{*;}


################gson##################
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class cn.jcyh.eagleking.bean.**{ *;}
#jpush
-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

#热修复
-dontwarn android.annotation
-dontwarn com.alipay.euler.**
-keep class com.alipay.euler.** {*;}
-keep class * extends java.lang.annotation.Annotation
-keepclasseswithmembernames class * {
    native <methods>;
}

#讯飞
-dontwarn com.iflytek.**
-keep class com.iflytek.**{*;}
-keepattributes Signature

#
#-----------------------
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep public class * implements java.io.Serializable {*;}
-keep public class com.nineoldandroids.** {*;}

# support-v4
#https://stackoverflow.com/questions/18978706/obfuscate-android-support-v7-widget-gridlayout-issue
-dontwarn android.support.v4.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }

# support-v7
-dontwarn android.support.v7.**
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }
# picasso
-keep class com.squareup.picasso.** {*; }
-dontwarn com.squareup.picasso.**
# support design
#@link http://stackoverflow.com/a/31028536
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
#activeandroid
-keep class com.activeandroid.** { *; }
-dontwarn com.ikoding.app.biz.dataobject.**
-keep public class com.ikoding.app.biz.dataobject.** { *;}
-keepattributes *Annotation*
#log4j
-dontwarn org.apache.log4j.**
-keep class  org.apache.log4j.** { *;}
-keep class com.fbee.zllctl.** {*;}
-dontwarn com.fbee.zllctl.**
#anychat
-keep class com.bairuitech.anychat.** {*;}
-dontwarn com.bairuitech.anychat.**
#技威
-keep class com.example.p2p_core.**{ *;}
-dontwarn com.example.p2p_core.**
-keep class com.p2p.**{ *;}
-dontwarn com.p2p.**
#auxdio
-keep class cn.com.auxdio.protocol.**{*;}
-dontwarn cn.com.auxdio.protocol.**
#友盟
-keep class com.umeng.** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class com.szjcyh.mysmartx.R$*{
public static final int *;
}
#greendao
-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
-dontwarn org.greenrobot.greendao.**

-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

-keep class com.huawei.gamebox.plugin.gameservice.**{*;}

-keep public class com.huawei.android.hms.agent.** extends android.app.Activity { public *; protected *; }
-keep interface com.huawei.android.hms.agent.common.INoProguard {*;}
-keep class * extends com.huawei.android.hms.agent.common.INoProguard {*;}



#freemarker
-keep class freemarker.**{*;}
-dontwarn freemarker.**
-keep class **$Properties
#-------------------------------------------------------------------------

#---------------------------------3.与js互相调用的类------------------------



#-------------------------------------------------------------------------

#---------------------------------4.反射相关的类和方法-----------------------



#----------------------------------------------------------------------------
#---------------------------------------------------------------------------------------------------

#-------------------------------------------基本不用动区域--------------------------------------------
#---------------------------------基本指令区----------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------

#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}

-keep public class * extends android.support.v7.**
#----------------------------------------------------------------------------

#---------------------------------webview------------------------------------
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

#tinker------------------------------------
#-applymapping "old apk mapping here"

-keepattributes *Annotation*
-dontwarn com.tencent.tinker.anno.AnnotationProcessor
-keep @com.tencent.tinker.anno.DefaultLifeCycle public class *
-keep public class * extends android.app.Application {
    *;
}

-keep public class com.tencent.tinker.loader.app.ApplicationLifeCycle {
    *;
}
-keep public class * implements com.tencent.tinker.loader.app.ApplicationLifeCycle {
    *;
}

-keep public class com.tencent.tinker.loader.TinkerLoader {
    *;
}
-keep public class * extends com.tencent.tinker.loader.TinkerLoader {
    *;
}

-keep public class com.tencent.tinker.loader.TinkerTestDexLoad {
    *;
}

-keep public class com.tencent.tinker.loader.TinkerTestAndroidNClassLoader {
    *;
}

#for command line version, we must keep all the loader class to avoid proguard mapping conflict
#your dex.loader pattern here
-keep public class com.tencent.tinker.loader.** {
    *;
}

-keep class tinker.sample.android.app.SampleApplication {
    *;
}