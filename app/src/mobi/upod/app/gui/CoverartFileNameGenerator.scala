package mobi.upod.app.gui

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator
import mobi.upod.util.Hashing

class CoverartFileNameGenerator extends FileNameGenerator {
  override def generate(imageUri: String): String = Hashing.sha256Hex(imageUri)
}
