package mobi.upod.app.services.playback.player

import android.annotation.TargetApi
import mobi.upod.android.util.ApiLevel

import android.media.{MediaPlayer => AndroidMediaPlayerClass}

@TargetApi(MediaPlayerCompat.MIN_LEVEL)
class MediaPlayerCompat extends AndroidMediaPlayerClass {
  private var volumeLeft: Float = 1f
  private var volumeRight: Float = 1f
  private var gain: Float = 1f

  def isSetSpeedSupported: Boolean = ApiLevel >= MediaPlayerCompat.MIN_LEVEL

  override def setVolume(leftVolume: Float, rightVolume: Float): Unit = {
    super.setVolume(leftVolume, rightVolume)
    volumeLeft = leftVolume
    volumeRight = rightVolume
  }

  def getSpeed: Float = if (isSetSpeedSupported) getPlaybackParams.getSpeed else 1f

  def setSpeed(multiplier: Float): Unit = ifSupported(() => setPlaybackParams(getPlaybackParams.setSpeed(multiplier)))

  def getGain: Float = gain

  def setGain(value: Float): Unit = ifSupported(() => {
    gain = value
    setVolume(volumeLeft + value, volumeRight + value)
  })

  private def ifSupported(block: () => Unit): Unit = {
    if (isSetSpeedSupported) {
      block()
    } else {
      throw new UnsupportedOperationException
    }
  }
}

object MediaPlayerCompat {
  final val MIN_LEVEL = ApiLevel.Marshmallow
}