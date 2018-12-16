package mobi.upod.util

import java.security.MessageDigest

import android.util.Base64
import mobi.upod.io.Charset

object Hashing {
  private val digest = MessageDigest.getInstance("SHA-256")

  def sha256(str: String): String = {
    val hash = digest.synchronized {
      digest.digest(str.getBytes(Charset.utf8))
    }
    new String(Base64.encode(hash, Base64.NO_WRAP))
  }
}
