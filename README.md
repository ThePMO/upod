# About uPod
uPod is a full blown podcast player for Android. It's key features are:

- Audio playback with speed control and volume gain
- Video playback (including audio only option)
- Chapter support for MP3 and M4A/AAC/MP4 files
- Support for bluetooth headsets and headset controls
- Offline show notes
- Drag-and-drop playlist
- Coverart centric, podcast colored user interface
- Effective inbox workflow for assorting new episodes²
- Full automatic download
- Resumable downloads
- Automatic media file cleanup
- Lock screen and notification playback controls
- Support for Android Wear (control playback from your watch)
- Podcast subscription management
- Extensive podcast catalog with search capabilities
- OPML import and export
- Responsive layout (looking great on phone and tablet support)
- Material design (Lollipop/Android 5)
- English and German translations

Originally uPod was separated into a free and a premium version. The code provided here contains all the features. To enable them all you should at first disable the premium check. The premium features are:

- Cloud backup and cross device sync of playback positions, playlist and episode states tied to your Google account
- Chromecast support
- Chapter navigation (MP3 and M4A)
- Sleep timer (stop and end of chapter, episode or after some time)
- Podcast specific settings (e.g. auto add to playlist, only keep _x_ latest episodes)
- up to two specific sync times per day

# Why now at github?
I have no longer time to maintain uPod. Further on I've switched over to the dark side and am using iOS devices now. uPod contains to much know-how to get lost. That's why I publish the code here.

# Building uPod
uPod has been developed using Scala which has proven to be a bad choice for Android development. Though I love Scala, the Scala support on the build site for Android projects is bad and lots of the projects which where dedicated to solving this problem are no longer maintained -- especially since Kotlin has been announced as an officially supported, modern language for Android. So building uPod is an adventure -- unfortunately.

## Missing information
Some of the information in some of the build and source files is information related to private API accounts and thus has been removed by me. You can find all the missing stuff by searching for the string `???`.

## 3rd party Dependencies
Unfortunately I needed to put some of the 3rd party dependencies into the project structure to make the whole application build.

## Development Builds
For developing uPod you can build using sbt which is fast and comfortable (when it works). uPod uses [sbt-android](https://github.com/scala-android/sbt-android) for this purpose.

## Release Builds
Unfortunately I never managed it to create release builds using sbt-android. Thus for release builds you need to use gradle. I include the `gradle-android-scala-plugin` to compile the Scala code.
