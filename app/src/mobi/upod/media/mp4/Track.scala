package mobi.upod.media.mp4

import org.mp4parser.boxes.iso14496.part12.TrackBox


class Track private[mp4] (trackBox: TrackBox) {
  val handlerType: String = trackBox.getMediaBox.getHandlerBox.getHandlerType
  val name: String = trackBox.getMediaBox.getHandlerBox.getName
  lazy val sampleInfoTable: SampleTable[SampleInfo] = SampleTable(SampleInfo.listFrom(trackBox.getMediaBox.getMediaHeaderBox.getTimescale, trackBox.getSampleTableBox))
}

object Track {
  object HandlerType {
    val Text = "text"
    val Video = "vide"
  }
}