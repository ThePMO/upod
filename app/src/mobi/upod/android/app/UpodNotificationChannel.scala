package mobi.upod.android.app

import android.annotation.TargetApi
import android.app.{Application, NotificationChannel, NotificationManager}
import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import de.wcht.upod.R
import mobi.upod.android.util.ApiLevel

import scala.collection.JavaConversions

@TargetApi(ApiLevel.Oreo)
sealed abstract class UpodNotificationChannel(val channelId: String,
                                              @StringRes private val nameId: Int,
                                              @StringRes private val descriptionId: Int,
                                              val importance: Int = NotificationManager.IMPORTANCE_LOW) {
  def createAndroidChannel(app: Application): NotificationChannel = {
    val channel = new NotificationChannel(channelId, app.getString(nameId), importance)
    channel.setDescription(app.getString(descriptionId))
    channel
  }
}

@TargetApi(ApiLevel.Oreo)
object UpodNotificationChannels {

  case object Default extends UpodNotificationChannel("upod_default", R.string.default_channel_name, R.string.default_channel_description,
    importance = NotificationManager.IMPORTANCE_DEFAULT)

  case object Sync extends UpodNotificationChannel("upod_sync", R.string.sync_channel_name, R.string.sync_channel_description)

  case object Download extends UpodNotificationChannel("upod_download", R.string.download_channel_name, R.string.download_channel_description)

  case object Episode extends UpodNotificationChannel("upod_episode", R.string.episode_channel_name, R.string.episode_channel_description)

  case object NewEpisode extends UpodNotificationChannel("upod_new_episode", R.string.new_episode_channel_name, R.string.new_episode_channel_description)

  private val ALL_CHANNELS = List(Default, Sync, Download, Episode, NewEpisode)

  def initNotificationChannels(app: Application, notificationManager: NotificationManager): Unit = {
    if (ApiLevel < ApiLevel.Oreo) {
      return
    }

    val channels = ALL_CHANNELS.map(_.createAndroidChannel(app))
    notificationManager.createNotificationChannels(JavaConversions.seqAsJavaList(channels))
  }

  class DefaultChannelNotificationBuilder(context: Context, channel: UpodNotificationChannel) extends NotificationCompat.Builder(context, channel.channelId)
}
