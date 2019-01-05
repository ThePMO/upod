package mobi.upod.app.services

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import mobi.upod.android.os.AsyncTask
import mobi.upod.app.data.Announcement
import mobi.upod.app.storage.AnnouncementDao

final class AnnouncementService(implicit val bindingModule: BindingModule) extends Injectable {
  private val dao = inject[AnnouncementDao]
  private var announcement: Option[Announcement] = None

  def init(): Unit = {
    updateCurrentAnnouncement()
  }

  def currentAnnouncement: Option[Announcement] = announcement

  def dismissAnnouncement(id: Long): Unit = {
    if (announcement.exists(_.id == id)) {
      announcement = None
    }
    AsyncTask.execute {
      dao.inTransaction(dao.setDismissed(id))
      if (announcement.isEmpty)
        findNextAnnouncement
      else
        announcement
    } (announcement = _)
  }

  def onNewAnnouncement(): Unit = {
    announcement = findNextAnnouncement
  }

  private def findNextAnnouncement: Option[Announcement] =
    dao.findNextAnnouncement()

  private def updateCurrentAnnouncement(): Unit =
    AsyncTask.execute(findNextAnnouncement)(announcement = _)
}