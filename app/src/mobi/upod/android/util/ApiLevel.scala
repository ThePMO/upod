package mobi.upod.android.util

import android.os.Build.VERSION_CODES._

object ApiLevel extends Ordered[Int] {
  final val IceCreamSandwich = ICE_CREAM_SANDWICH
  final val IceCreamSandwichM1 = ICE_CREAM_SANDWICH_MR1
  final val JellyBean = JELLY_BEAN
  final val JellyBeanM1 = JELLY_BEAN_MR1
  final val JellyBeanM2 = JELLY_BEAN_MR2
  final val KitKat = KITKAT
  final val Lollipop = LOLLIPOP
  final val Marshmallow = M
  final val Nougat = N
  final val NougatMR1 = N_MR1
  final val Oreo = O

  val apiLevel = android.os.Build.VERSION.SDK_INT

  def compare(that: Int) = apiLevel - that
}
