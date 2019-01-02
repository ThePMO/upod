package mobi.upod.app.gui.playback

import android.content.Context
import android.view._
import android.widget.AdapterView.OnItemClickListener
import android.widget.{AdapterView, TextView}
import com.escalatesoft.subcut.inject.BindingModule
import mobi.upod.android.app.action.{Action, ActionController, ActionState}
import mobi.upod.android.widget.bottomsheet.BottomSheet
import mobi.upod.android.widget.{ActionButtons, TintableProgressBar}
import tech.olbrich.upod.R
import mobi.upod.app.data.{EpisodeBaseWithPlaybackInfo, EpisodeListItem}
import mobi.upod.app.gui.chapters.{ChapterBottomSheetController, OpenChapterLinkAction}
import mobi.upod.app.services.playback.{PlaybackListener, PlaybackService}
import mobi.upod.app.storage.{InternalAppPreferences, PlaybackPreferences}
import mobi.upod.media.{MediaChapter, MediaChapterTable}
import mobi.upod.util.{MediaFormat, MediaPosition}

private[playback] class PlaybackChapterBottomSheetController(
    context: Context,
    episode: EpisodeListItem,
    chapters: MediaChapterTable,
    bottomSheet: BottomSheet)(
    implicit bindingModule: BindingModule)
  extends ChapterBottomSheetController(context, episode, chapters, bottomSheet)
  with ActionController
  with ActionButtons
  with PlaybackListener {

  private val playbackService = inject[PlaybackService]
  private val internalAppPreferences = inject[InternalAppPreferences]
  private val playbackPreferences = inject[PlaybackPreferences]
  private val chapterBar = childViewGroup(R.id.chapterBar)
  private val chapterIconView = chapterBar.childView(R.id.chapterIcon)
  private val chapterTitleView = chapterBar.childAs[TextView](R.id.chapterTitle)
  private val chapterTimeView = chapterBar.childTextView(R.id.chapterTime)
  private val progressBar = chapterBar.childAs[TintableProgressBar](R.id.chapterProgress)
  private var currentChapter: Option[MediaChapter] = None

  override protected val createActions: Map[Int, Action] = Map(
    R.id.openChapterLink -> new OpenChapterLinkAction(currentChapter.flatMap(_.link)),
    R.id.chapterSkip -> Action(playbackService.skipChapter(), playbackService.canSkipChapter, ActionState.gone),
    R.id.chapterBack -> Action(playbackService.goBackChapter(), playbackService.canGoBackChapter, ActionState.gone)
  )

  override protected def onCreate(): Unit = {
    initActionButtons(chapterBar)

    chapterList.show()

    chapterBar.setBackgroundColor(dimmedBackgroundColor)
    chapterIconView.onClick(toggleBottomSheet())
    progressBar.setTint(0xffffffff)
    chapterList.setOnItemClickListener(new OnChapterClickListener)

    onChapterChanged(None) // ensure that visibility and content of all controls is set correctly
    playbackService.addListener(this)
    onPlaybackPositionChanged(episode)

    super.onCreate()
  }

  private def onChapterChanged(chapter: Option[MediaChapter]): Unit = {
    chapterTitleView.setText(chapter.flatMap(_.title).getOrElse(""))
    progressBar.show(chapter.nonEmpty)
    chapter foreach { c =>
      progressBar.setMax(c.duration.toInt)
      progressBar.setSecondaryProgress(c.duration.toInt) // use secondary progress as track color
    }
    chapterListAdapter.onChapterChanged(chapter)
  }

  private def updateChapterProgress(chapter: Option[MediaChapter], mediaPos: Long): Unit = {
    chapter foreach { c =>
      val chapterPos = MediaPosition(mediaPos - c.startMillis, c.duration)
      progressBar.setProgress(chapterPos.position.toInt)

      val formattedPosition = MediaFormat.formatFullPosition(chapterPos, playbackPreferences.mediaTimeFormat.get, true)
      chapterTimeView.setText(formattedPosition)
    }
  }

  //
  // OnItemClickListener
  //

  private class OnChapterClickListener extends OnItemClickListener {

    override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit =
      playbackService.seek(id, true)
  }

  //
  // PlaybackListener
  //

  override def onPlaybackPositionChanged(episode: EpisodeBaseWithPlaybackInfo): Unit = {
    val pos = episode.playbackInfo.playbackPosition
    val newChapter = chapters.chapterAt(pos)
    if (newChapter != currentChapter) {
      currentChapter = newChapter
      onChapterChanged(newChapter)
    }
    updateChapterProgress(newChapter, pos)
    invalidateActionButtons()
  }
}