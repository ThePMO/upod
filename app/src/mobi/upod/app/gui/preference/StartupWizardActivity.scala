package mobi.upod.app.gui.preference

import android.app.Activity
import de.wcht.upod.R
import mobi.upod.android.view.wizard.{ValueChoice, _}
import mobi.upod.app.AppInjection
import mobi.upod.app.gui.MainActivity
import mobi.upod.app.gui.preference.StartupWizardActivity.ExistingUser
import mobi.upod.app.storage._

final class StartupWizardActivity extends WizardActivity with StoragePermissionRequestActivity with AppInjection {

  import mobi.upod.app.gui.preference.StartupWizardActivity._

  override protected def hasNextPage(currentPageIndex: Int, currentPageKey: String): Boolean = currentPageKey match {
    case PageKeyWelcome => true
    case _ => false
  }

  override protected def createFirstPage: WizardPage =
    new WelcomePage

  override protected def createNextPage(currentPageIndex: Int, currentPageKey: String): WizardPage = currentPageKey match {
    case PageKeyWelcome => userType match {
      case Some(NewUser) => new NewUserPage
      case Some(ExistingUser) => new RecurringUserPage
      case _ => new RecurringUserPage
    }
    case _ => new RecurringUserPage
  }

  override protected def onFinishButtonClicked(): Unit = {
    ensureExternalStoragePermission(true)
  }

  protected def onFinish(): Unit = {
    val preferences = inject[InternalAppPreferences]
    preferences.showStartupWizard := false
  }

  override protected val followUpActivity =
    classOf[MainActivity]

  override protected val requestPermissionOnStart: Boolean = false

  override protected def shouldRequestExternalStoragePermission: Boolean = true

  override protected[preference] def onStorageAvailable(): Unit = {
    super.onStorageAvailable()
    finishWizard()
  }
}

object StartupWizardActivity {
  private val PageKeyWelcome = "welcome"
  private val PageKeyNewUser = "newUser"
  private val PageKeyRecurringUser = "recurringUser"

  private val NewUser = 0
  private val ExistingUser = 1

  private var userType: Option[Int] = None

  def start(activity: Activity): Unit = {
    WizardActivity.start(activity, classOf[StartupWizardActivity])
  }

  def startInsteadOf(activity: Activity): Unit = {
    activity.finish()
    start(activity)
  }

  class WelcomePage extends SimpleSingleChoicePage[Int](
    PageKeyWelcome,
    R.string.wizard_welcome,
    R.string.wizard_welcome_introduction,
    0,
    userType,
    choice => userType = Some(choice),
    ValueChoice(NewUser, R.string.wizard_welcome_option_new_user),
    ValueChoice(ExistingUser, R.string.wizard_welcome_option_existing_user)
  )

  class NewUserPage extends WizardWebPage(PageKeyNewUser, R.string.wizard_new_user, R.string.wizard_new_user_introduction)

  class RecurringUserPage extends WizardWebPage(PageKeyRecurringUser, R.string.wizard_recurring_user, R.string.wizard_recurring_user_introduction)
}
