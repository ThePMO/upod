package mobi.upod.util

import java.security.MessageDigest

import android.util.Base64
import com.google.common.io.BaseEncoding
import mobi.upod.io.Charset

object Hashing {
  private val digest = MessageDigest.getInstance("SHA-256")

  def sha256Base64(str: String): String = {
    new String(Base64.encode(sha256Bytes(str), Base64.NO_WRAP))
  }

  def sha256Hex(str: String): String = {
    new String(BaseEncoding.base16().encode(sha256Bytes(str))).toLowerCase()
  }

  private def sha256Bytes(str: String) = {
    digest.synchronized {
      digest.digest(str.getBytes(Charset.utf8))
    }
  }
}
