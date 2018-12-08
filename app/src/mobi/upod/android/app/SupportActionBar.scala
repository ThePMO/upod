package mobi.upod.android.app

import android.app.Activity
import android.support.v7.app.{ActionBar, AppCompatActivity}

trait SupportActionBar {

  def getActivity: Activity

  def supportActionBar: ActionBar =
    getActivity.asInstanceOf[AppCompatActivity].getSupportActionBar
}
