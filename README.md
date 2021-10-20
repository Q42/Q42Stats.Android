# Q42Stats.Android 
[![Release](https://jitpack.io/v/Q42/Q42Stats.Android.svg)](https://jitpack.io/#Q42/Q42Stats.Android)
[![](https://jitci.com/gh/Q42/Q42Stats.Android/svg)](https://jitci.com/gh/Q42/Q42Stats.Android)


Collect stats for Q42 internal usage, shared across multiple Android projects.

An iOS version is also available: https://github.com/Q42/Q42Stats

## Installation
Add the Jitpack repo and include the library:

```gradle
   allprojects {
       repositories {
           [..]
           maven { url "https://jitpack.io" }
       }
   }
   dependencies {
       implementation 'com.github.q42:q42stats.android:X.X.X' // Replace X.X.X by the latest version,
       // which is available in the Jitpack badge at the top of this page
   }
```  

## Usage

Call `Q42Stats().runAsync(Context)` from anywhere in your app. 
```kotlin
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Q42Stats(
            Q42StatsConfig(
                firebaseProjectId = "theProject",
                firestoreCollectionId = "theCollection",
                // wait at least 7.5 days between data collections. the extra .5 is for time-of-day randomization
                minimumSubmitIntervalSeconds = (60 * 60 * 24 * 7.5).toLong()
            )
        ).runAsync(this.applicationContext)
    }
}
```
This can be safely called from the main thread since all work (both collecting statistics and sending them to the server) are done on an IO thread. 

It is safe to call this function multiple times, as it will exit immediately if it is already running or when a data collection interval has not passed yet.

By default, Q42Stats only logs errors. For debugging purposes, set the log level before using Q42Stats:

```
Q42Stats.logLevel = Q42StatsLogLevel.Debug
```

## Data collected

Not all fields are supported on all versions of Android. If unsupported, the corresponding key is omitted.

### Accessibliity

| Key | Value | Notes |
|-|-|-|
| `isAccessibilityManagerEnabled` | bool | true when any accessibility service (eg. Talkback) is Enabled | 
| `isClosedCaptioningEnabled` | bool | Live transcription of any spoken audio (min sdk 19) |
| `isTouchExplorationEnabled` | bool | Wehether any assistive feature is enabled where the user navigates the interface by touch. Most probably TalkbBack, or similar 
| `isTalkBackEnabled` | bool | iOS: VoiceOver
| `isSamsungTalkBackEnabled` | bool | Specifically checks whether com.samsung.android.app.talkback.talkbackservice is enabled
| `isVoiceAccessEnabled` | bool | iOS: Voice Control
| `fontScale` | float | 1.0 is regular scaling |
| `displayScale` | float | Overall interface scaling ie. display density scaling. 1.0 is regular scaling (minSdk 24)|
| `enabledAccessibilityServices` | Array\<String\> | List of enabled accessibility package names, eg ['com.accessibility.service1', 'nl.accessibility.service2'] |

### Preferences

| Key | Value | Notes |
|-|-|-|
| `daytime`| day, twilight, night, unknown | Coarse estimation of time of day. unknown if user is not in Amsterdam TimeZone
| `isNightModeEnabled` | bool | iOS: Dark Mode (minSdk: 29)

### Screen

| Key | Value | Notes |
|-|-|-|
| `screenOrientation`| portrait, landscape, unknown |

### System

| Key | Value | Notes |
|-|-|-|
| `defaultLanguage`| en, nl, ... |
| `sdkVersion` | int | 29 for Android 10. [See this list](https://source.android.com/setup/start/build-numbers)


## Development

### Setup
This project contains a demo app which can simply be run without further setup. By default it tries to send data to a non-existing Firestore database, so change the SampleApplication to construct a Q42Stats object for a real database if you want to test server interaction.

### Publishing

This library is distributed using [JitPack](https://jitpack.io/#q42/q42stats.android). This makes publishing a new version very easy:

1. Push the code for the new version to the `main` branch
1. Unit tests will be run automatically. Check [JitCI](https://jitci.com/gh/Q42/Q42Stats.Android) for status
1. Create a tag in the semver format: `x.x.x` without the preceding `v.`
1. On GitHub, create a release from that tag. Give it the same name; `x.x.x`
1. If everything went well the release will be visible on [JitPack](https://jitpack.io/#q42/q42stats.android) and the version number in the badge at the top of this page will update.


