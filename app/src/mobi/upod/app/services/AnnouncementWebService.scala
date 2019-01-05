package mobi.upod.app.services

import java.util.Locale

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.google.api.client.http.HttpStatusCodes
import mobi.upod.app.AppMetaData
import mobi.upod.app.data._
import mobi.upod.net._
import mobi.upod.rest.WebService
import mobi.upod.util.Cursor

class AnnouncementWebService(implicit val bindingModule: BindingModule)
  extends WebService with Injectable {

  protected val baseUrl: String = inject[AppMetaData].upodServiceUrl

  def getAnnouncements(oldETag: Option[String]): (String, Cursor[Announcement]) = {
    val url = url"v1/announcement.json" withQueryParameters ("lang" -> Locale.getDefault.getLanguage)
    val response = get(url, headers = ("If-None-Match", oldETag))
    val eTag = response.response.getHeaders.getETag
    if (response.response.getStatusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
      return (eTag, Cursor.empty)
    }
    (eTag, response asStreamOf Announcement)
  }
}

