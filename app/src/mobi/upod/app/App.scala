package mobi.upod.app

import android.app.{NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.support.multidex.MultiDexApplication
import de.wcht.upod.R
import mobi.upod.android.app.{AppException, AppNotificationBuilder, UpodNotificationChannels}
import mobi.upod.android.logging.{LogConfiguration, Logging}
import mobi.upod.app.storage._
import org.joda.time.DateTime

final class App extends MultiDexApplication with Logging {
  implicit lazy val bindingModule = createBindingModule()

  private def createBindingModule(): AppDependencies = {
    // loading binding module via reflection here, to avoid
    // "Too many classes in --main-dex-list, main dex capacity exceeded" error
    val cls = Class.forName("mobi.upod.app.AppBindingModule")
    val constructor = cls.getConstructor(classOf[App])
    constructor.newInstance(this).asInstanceOf[AppDependencies]
  }

  override def onCreate() {
    new DateTime(0) // initialize Joda to avoid later deadlock
    Thread.currentThread().setContextClassLoader(getClass.getClassLoader) // initialize properties loading for ROME RSS

    super.onCreate()

    UpodNotificationChannels.initNotificationChannels(this, notificationManager)

    App.app = Some(this)
    UncaughtExceptionHandler.install(this)

    LogConfiguration.configureLogging(this, new SupportPreferences(this).enhancedLogging)

    checkForUpgrade()
    bindingModule.onAppCreate()
  }

  override def onTerminate() {
    bindingModule.onAppTerminate()
    super.onTerminate()
  }

  def appVersion: Int =
    getPackageManager.getPackageInfo(getPackageName, 0).versionCode

  def notifyError(ex: AppException): Unit = {
    val pendingIntent = ex.intent.map(i => PendingIntent.getActivity(this, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK))
    notifyError(ex.getClass.getSimpleName, ex.errorTitle(this), ex.errorText(this), pendingIntent)
  }

  def notifyError(tag: String, title: CharSequence, content: CharSequence, intent: Option[PendingIntent] = None): Unit = {
    val builder = new AppNotificationBuilder(this, UpodNotificationChannels.Default)
    builder.
      setSmallIcon(R.drawable.ic_stat_error).
      setContentTitle(title).
      setContentText(content)
    intent.foreach(builder.setContentIntent)
    notificationManager.notify(tag, 1, builder.build)
  }

  def cancelErrorNotification(tag: String): Unit =
    notificationManager.cancel(tag, 1)

  private def checkForUpgrade(): Unit = {
    val appVersionPreference = bindingModule.inject[InternalAppPreferences](None).appVersion
    val oldVersion = appVersionPreference.get
    val newVersion = appVersion
    if (newVersion > oldVersion) {
      if (oldVersion > 0) {
        onUpgrade(oldVersion, newVersion)
      }
      appVersionPreference := newVersion
    }
  }

  private def onUpgrade(oldVersion: Int, newVersion: Int): Unit = {
    log.crashLogInfo(s"app upgraded from $oldVersion to $newVersion")
    bindingModule.onUpgrade(oldVersion, newVersion)
  }

  private def notificationManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
}

object App {
  private var app: Option[App] = None

  lazy val instance: App = app.get

  def inject[A](implicit manifest: scala.Predef.Manifest[A]): A =
    instance.bindingModule.inject[A](None)
}
