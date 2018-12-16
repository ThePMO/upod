package mobi.upod.io

import java.io.File

import android.content.Context
import android.net.Uri

object WorldReadables {
  def worldReadable(context: Context, file: File): Uri = {
    val targetDir = new File(context.getFilesDir, "sharables")
    targetDir.mkdir()
    file.copyToDir(targetDir)
    Uri.parse(s"content://de.wcht.upod.fileprovider/sharables/${file.getName}")
  }

  def worldReadable(context: Context, files: Iterable[File]): Iterable[Uri] =
    files.map(worldReadable(context, _))
}
