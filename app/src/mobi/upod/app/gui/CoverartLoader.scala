package mobi.upod.app.gui

import java.io.File
import java.net.URL

import android.widget.ImageView
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import com.nostra13.universalimageloader.core.{DisplayImageOptions, ImageLoader, ImageLoaderConfiguration}
import com.nostra13.universalimageloader.utils.StorageUtils
import mobi.upod.android.logging.Logging
import mobi.upod.android.view.DisplayMetrics
import mobi.upod.app.App
import mobi.upod.app.services.net.ConnectionStateRetriever
import mobi.upod.app.storage.{CoverartProvider, DownloadPreferences, ImageSize}
import mobi.upod.util.StorageSize.IntStorageSize

class CoverartLoader(implicit val bindingModule: BindingModule) extends Injectable with Logging {
  private lazy val coverartProvider = inject[CoverartProvider]
  private lazy val imageLoader = initImageLoader()
  private lazy val connectionService = inject[ConnectionStateRetriever]
  private lazy val downloadPreferences = inject[DownloadPreferences]
  private implicit lazy val displayMetrics = DisplayMetrics(inject[App])

  private lazy val baseDisplayImageOptionsBuilder = new DisplayImageOptions.Builder()
    .displayer(new FadeInBitmapDisplayer(500))
    .cacheInMemory(true)
  private lazy val localDefaultDisplayImageOptions = baseDisplayImageOptionsBuilder.build()
  private lazy val localListDisplayImageOptions = baseDisplayImageOptionsBuilder.cacheInMemory(true).build()
  private lazy val onlineDisplayImageOptions = baseDisplayImageOptionsBuilder.cacheOnDisk(true).build()

  private def initImageLoader(): ImageLoader = {
    val app = inject[App]
    val cacheDir = new File(StorageUtils.getCacheDirectory(app, false), "coverart")
    val config = new ImageLoaderConfiguration.Builder(app)
      .imageDownloader(new BaseImageDownloader(app))
      .defaultDisplayImageOptions(localDefaultDisplayImageOptions)
      .memoryCache(new LruMemoryCache(1.mb))
      .diskCache(new LruDiskCache(cacheDir, null, new CoverartFileNameGenerator, 0, 1000))
      .build()

    val imageLoader = ImageLoader.getInstance
    imageLoader.init(config)
    imageLoader
  }

  def displayImage(view: ImageView, size: ImageSize, url: Option[URL], fallback: CoverartLoaderFallbackDrawable): Unit = url match {
    case Some(u) if u.toString.nonEmpty => displayImage(view, size, u, fallback)
    case _ => view.setImageDrawable(fallback)
  }

  private def displayImage(view: ImageView, size: ImageSize, url: URL, fallback: CoverartLoaderFallbackDrawable): Unit = {

    val (uri, showFallback) = getImageUrl(size, url)

    try {
      val displayOptions = (showFallback, size) match {
        case (false, ImageSize.list) => localListDisplayImageOptions
        case (false, _) => localDefaultDisplayImageOptions
        case (true, _) => onlineDisplayImageOptions
      }
      if (showFallback) {
        // we're going to load an online image which may take a while, so draw a placeholder if available
        view.setImageDrawable(fallback)
      }
      uri.foreach(imageLoader.displayImage(_, view, displayOptions, fallback))
    } catch {
      case _: OutOfMemoryError =>
        log.error(s"failed to display image $uri due to OutOfMemoryError")
    }
  }

  private def getImageUrl(size: ImageSize, url: URL): (Option[String], Boolean) = {
    val existingImageFile = coverartProvider.getExistingImageFile(url, size)
    existingImageFile match {
      case Some(file) => Some("file://" + file.getAbsolutePath) -> false
      case None => chooseLiveOrFallbackUri(url)
    }
  }

  private def chooseLiveOrFallbackUri(url: URL): (Option[String], Boolean) = {
    if (connectionService.isUnmeteredConnection || downloadPreferences.allowDownloadOnAnyConnection()) {
      Some(url.toExternalForm) -> true
    } else {
      None -> true
    }
  }
}
