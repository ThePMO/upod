package mobi.upod.android.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

trait UpNavigation extends AppCompatActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case android.R.id.home =>
        navigateUp()
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

  protected def navigateUp()
}
