package mobi.upod.android.app

import android.app.{Application, NotificationChannel, NotificationManager}
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import de.wcht.upod.R

object NotificationChannels {
  val DEFAULT_CHANNEL_ID = "upod_default"

  private val NO_VIBRATION = Array[Long](0L)

  def createNotificationChannel(app: Application, notificationManager: NotificationManager): Unit = {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelName = app.getString(R.string.channel_name)
      val description = app.getString(R.string.channel_description)

      val channelId = NotificationChannels.DEFAULT_CHANNEL_ID
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = new NotificationChannel(channelId, channelName, importance)
      channel.setDescription(description)
      channel.setVibrationPattern(NO_VIBRATION)
      notificationManager.createNotificationChannel(channel)
    }
  }

  class DefaultChannelNotificationBuilder(context: Context) extends NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
}
