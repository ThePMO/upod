package mobi.upod.android.app

import android.content.Context
import android.support.v7.appcompat.R
import mobi.upod.android.app.UpodNotificationChannels.DefaultChannelNotificationBuilder

import scala.util.Try

class AppNotificationBuilder(context: Context, channel: UpodNotificationChannel) extends DefaultChannelNotificationBuilder(context, channel) {
  applyPrimaryColor()

  private def applyPrimaryColor(): Unit =
    Try(context.getApplicationContext.getResources.getColor(R.attr.colorPrimary))
}
