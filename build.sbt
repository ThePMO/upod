import android.Keys._
import android.DebugSigningConfig
import android.PlainSigningConfig
import java.util.Properties

val androidBuildToolsVersion = Some("26.0.1")
val androidPlatformTarget = "android-25"
val androidMinSdkVersion = "21" // should be 14 (21 for debug because of multi dex support)

resolvers in ThisBuild ++= List(
  Resolver.jcenterRepo,
  "Google Maven" at "https://maven.google.com/"
)

def appProperties(): Properties = {
  val prop = new Properties()
  IO.load(prop, new File("app/gradle.properties"))
  prop
}

val gradleAppVersionCode = appProperties().getProperty("VERSION_CODE").toInt
val gradleAppVersionName = appProperties().getProperty("VERSION_NAME")
val javacVersionOptions = Seq("-source", "1.7", "-target", "1.7")

//
// android libraries
//

val bottomSheet = project.in(file("bottom-sheet")).
  enablePlugins(AndroidLib).
  settings(
    name := "bottom-sheet",

    buildToolsVersion in Android := androidBuildToolsVersion,
    platformTarget in Android := androidPlatformTarget,
    minSdkVersion in Android := androidMinSdkVersion,
    javacOptions in Compile ++= javacVersionOptions,

    libraryDependencies ++= List(
      aar("com.android.support" % "appcompat-v7" % "25.3.1"),
      aar("com.android.support" % "design" % "25.3.1"),
      aar("com.android.support" % "support-v4" % "25.3.1")
    )
  )

val dragSortListView = project.in(file("drag-sort-listview")).
  enablePlugins(AndroidLib).
  settings(
    buildToolsVersion in Android := androidBuildToolsVersion,
    platformTarget in Android := androidPlatformTarget,
    minSdkVersion in Android := androidMinSdkVersion,
    javacOptions in Compile ++= javacVersionOptions,

    antLayoutDetector in Android := (),

    libraryDependencies ++= List(
      aar("com.android.support" % "appcompat-v7" % "25.3.1"),
      aar("com.android.support" % "support-v4" % "25.3.1")
    )
  )

val prestissimo = project.in(file("prestissimo")).
  enablePlugins(AndroidLib).
  settings(
    buildToolsVersion in Android := androidBuildToolsVersion,
    platformTarget in Android := androidPlatformTarget,
    minSdkVersion in Android := androidMinSdkVersion,
    javacOptions in Compile ++= javacVersionOptions,

    antLayoutDetector in Android := (),

    mappings in (Compile, packageBin) ++= {
     val jniLibs = baseDirectory.value / "jniLibs"
     jniLibs ** "*.so" pair rebase(jniLibs, "lib")
    },

    libraryDependencies ++= List(
      "org.slf4j" % "slf4j-api" % "1.7.25"
    )
  )

val showcaseView = project.in(file("showcase-view")).
  enablePlugins(AndroidLib).
  settings(
    buildToolsVersion in Android := androidBuildToolsVersion,
    platformTarget in Android := androidPlatformTarget,
    minSdkVersion in Android := androidMinSdkVersion,
    javacOptions in Compile ++= javacVersionOptions,

    libraryDependencies ++= List()
  )

//
// apps
//

val app = project.in(file("app")).
  enablePlugins(AndroidApp).dependsOn(dragSortListView, prestissimo, showcaseView, bottomSheet).
  //enablePlugins(AndroidProtify).
  settings(
    versionCode in Android := Some(gradleAppVersionCode),
    versionName in Android := Some(gradleAppVersionName),
    buildToolsVersion in Android := androidBuildToolsVersion,
    platformTarget in Android := androidPlatformTarget,
    minSdkVersion in Android := androidMinSdkVersion,

    antLayoutDetector in Android := (),

    unmanagedResourceDirectories in Compile := Seq(baseDirectory.value / "src"),
    includeFilter in unmanagedResources := "*.properties",

    javacOptions in Compile ++= javacVersionOptions,
    scalaVersion := "2.11.6",

    resConfigs in Android := Seq("en", "de"),

    apkDebugSigningConfig in Android := DebugSigningConfig(
      keystore = new File(sys.env.getOrElse("UPOD_DEBUG_KEYSTORE_LOCATION", "keystore-debug.jks"))
    ),
    apkSigningConfig in Android := Option(
      PlainSigningConfig(
        keystore = new File(sys.env.getOrElse("UPOD_RELEASE_KEYSTORE_LOCATION", "/no-default-value-for-release-keystore")),
        storePass = sys.env.getOrElse("UPOD_RELEASE_KEYSTORE_PASSWORD", ""),
        alias = sys.env.getOrElse("UPOD_RELEASE_KEYSTORE_KEY_ALIAS", ""),
        keyPass = sys.env.get("UPOD_RELEASE_KEYSTORE_KEY_PASSWORD")
      )
    ),

    libraryDependencies ++= List(
      aar("mobi.upod" % "time-duration-picker" % "1.0.3"),

      "com.escalatesoft.subcut" % "subcut_2.11" % "2.1",
      "com.evernote" % "android-job" % "1.1.12",
      "com.google.http-client" % "google-http-client-android" % "1.27.0",
      "com.google.code.gson" % "gson" % "2.8.5",
      "org.mp4parser" % "isoparser" % "1.9.37",
      "com.github.nscala-time" % "nscala-time_2.11" % "2.20.0",
      "com.nostra13.universalimageloader" % "universal-image-loader" % "1.9.5",

      aar("com.github.tony19" % "logback-android" % "1.3.0-3"),
      "org.slf4j" % "slf4j-api" % "1.7.25",

      aar("com.android.support" % "support-v13" % "25.3.1"),
      aar("com.android.support" % "design" % "25.3.1"),
      aar("com.android.support" % "cardview-v7" % "25.3.1"),
      aar("com.android.support" % "palette-v7" % "25.3.1"),
      aar("com.android.support" % "appcompat-v7" % "25.3.1"),
      "com.android.support" % "multidex" % "1.0.1"
    ),

    useProguardInDebug in Android := false,

    dexMulti in Android := true,
    dexMaxHeap in Android :="2g",
    dexMainClassesConfig in Android := baseDirectory.value / "maindexlist.txt",

    packagingOptions in Android := PackagingOptions(
      excludes = Seq(
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE",
        "META-INF/LICENSE.txt",
        "META-INF/license.txt",
        "META-INF/NOTICE",
        "META-INF/NOTICE.txt",
        "META-INF/notice.txt",
        "META-INF/ASL2.0",
        "rootdoc.txt"
      )
    )
  )
