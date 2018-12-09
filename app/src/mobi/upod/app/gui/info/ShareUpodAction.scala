package mobi.upod.app.gui.info

import mobi.upod.android.app.action.ShareAction
import mobi.upod.android.content.GooglePlay
import de.wcht.upod.R

class ShareUpodAction extends ShareAction(
  R.string.share_upod_title,
  context => Some(ShareAction.SharedData(
    context.getString(R.string.share_upod_body, GooglePlay.webUrl("mobi.upod.app")),
    context.getString(R.string.share_upod_subject))))
