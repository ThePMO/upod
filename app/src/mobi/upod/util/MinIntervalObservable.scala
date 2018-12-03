package mobi.upod.util

trait MinIntervalEventFilter[A] { self: Observable[A] =>
  protected val minIntervalMillis = MinIntervalObservable.DefaultInterval
  private var latestEventTime: Option[Long] = None

  def fireNonIntrusive(event: A => Unit) {
    val currentTime = System.currentTimeMillis()
    val allowed = latestEventTime.forall(_ + minIntervalMillis < currentTime)
    if (allowed) {
      latestEventTime = Some(currentTime)
      fire(event)
    }
  }
}

object MinIntervalObservable {
  val DefaultInterval = 1000
}
