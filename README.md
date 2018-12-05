# uPod OSS
## About
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
- Chapter navigation (MP3 and M4A)
- Sleep timer (stop and end of chapter, episode or after some time)
- Podcast specific settings (e.g. auto add to playlist, only keep _x_ latest episodes)
- up to two specific sync times per day

## Development

### Current state
After uPod was open-sourced my first goal was to get a working build, to be able to continue using uPod as long as possible.

The easiest way to achieve this was to get rid of some features that needed these services. This is why this Open Source version does not support neither cloud sync nor Chromecast at the moment. I don't don't have need for those features myself, but feel free to bring then back in a way that can be easily configured at build-time, the removal commits 6706ecfa1c4fe583b77592d92be69464930d873e and 6d860823cbbd70501a719e5f984db9795e93824a should be a good start.

Currently the application is building and seems to work fine, except for the missing features. The TODOs that Sven added in the initial release are done. This means that the license check is removed, giving everyone all available features. All call's of Sven's webservices are removed, which mainly means that the Announcement about uPods discontinuation is gone. If you were effected by the black-on-black bug in the episode descriptions, that should be fixed :)

The only thing I'm currently missing to do the switch to the OSS version is to find a good way to import all my data including the lists.

### Building upod

#### Debug build

If you have installed [sbt](https://www.scala-sbt.org/) a debug build should be as simple as running in the checkout
```
sbt clean android:package-debug
```

#### Release build

The release build requires gradle at the moment (see "History" section), specifically gradle >= 3.3 and <=4.1 is needed, the used scala-gradle-plugin is not not compatible with recent gradle versions:

```
./release.sh /path/to/keystore.jks keystore_key_alias /path/to/gradle-4.1/bin/gradle
```

## History
uPod is originally developed by [@svenwiegand](https://github.com/svenwiegand) who decided to stop developing in 2018, here is the original content of the README from the initial publication at [@svenwiegand/upod](https://github.com/svenwiegand/upod):

> I have no longer time to maintain uPod. Further on I've switched over to the dark side and am using iOS devices now. uPod contains to much know-how to get lost. That's why I publish the code here.
>
> ##### Building uPod
> uPod has been developed using Scala which has proven to be a bad choice for Android development. Though I love Scala, the Scala support on the build site for Android projects is bad and lots of the projects which where dedicated to solving this problem are no longer maintained -- especially since Kotlin has been announced as an officially supported, modern language for Android. So building uPod is an adventure -- unfortunately.
>
> ##### Missing information
> Some of the information in some of the build and source files is information related to private API accounts and thus has been removed by me. You can find all the missing stuff by searching for the string `???`.
>
> ##### 3rd party Dependencies
> Unfortunately I needed to put some of the 3rd party dependencies into the project structure to make the whole application build.
>
> ##### Development Builds
> For developing uPod you can build using sbt which is fast and comfortable (when it works). uPod uses [sbt-android](https://github.com/scala-android/sbt-android) for this purpose.
>
> ##### Release Builds
> Unfortunately I never managed it to create release builds using sbt-android. Thus for release builds you need to use gradle. I include the `gradle-android-scala-plugin` to compile the Scala code.
