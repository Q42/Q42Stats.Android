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

1. Get the API key
   from [The Api project](https://github.com/Q42/accessibility-pipeline/tree/main/api). Use this key
   in the next step.

1. Call `Q42Stats().runAsync(Context)` from anywhere in your app.
    ```kotlin
    class SampleApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            Q42Stats(
                Q42StatsConfig(
                    apiKey = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                    firestoreCollectionId = "theCollection",
                    // wait at least 7.5 days between data collections. the extra .5 is for time-of-day randomization
                    minimumSubmitIntervalSeconds = (60 * 60 * 24 * 7.5).toLong()
                )
            ).runAsync(this.applicationContext)
        }
    }
    ```
   This can be safely called from the main thread since all work (both collecting statistics and
   sending them to the server) are done on an IO thread.

   It is safe to call this function multiple times, as it will exit immediately if it is already
   running or when a data collection interval has not passed yet.

### Debug Logging

By default, Q42Stats only logs errors. For debugging purposes, set the log level before using
Q42Stats:

```
Q42Stats.logLevel = Q42StatsLogLevel.Debug
```

## Performance

- Q42Stats is tiny, because it only depends on Kotlin. The library size is about 50kB.
- Data consumption is also very modest. For each `minimumSubmitInterval` that you configure, about
  5kB of data is transferred.
- Data collection is run on an IO thread, so it doesn't block your application.

## Data collected

Q42Stats does not collect any personally identifiably information and is fully GDPR compliant. This
has been verified by legal counsel. It should not be necesaary to ask users for permission before
invoking Q42Stats.

Below is a listing of all information gathered by Q42Stats. Not all fields are supported on all
versions of Android. If unsupported, the corresponding key is omitted.

### Accessibliity

| Key | Value | Notes |
|-|-|-|
| `isAccessibilityManagerEnabled` | bool | true when any accessibility service (eg. Talkback) is Enabled | 
| `isClosedCaptioningEnabled` | bool | Live transcription of any spoken audio (min sdk 19) |
| `isTouchExplorationEnabled` | bool | Whether any assistive feature is enabled where the user navigates the interface by touch. Most probably TalkBack, or similar
| `isTalkBackEnabled` | bool | iOS: VoiceOver
| `isSamsungTalkBackEnabled` | bool | Specifically checks whether com.samsung.android.app.talkback.talkbackservice is enabled
| `isSelectToSpeakEnabled` | bool | iOS: Speak Selection
| `isSwitchAccessEnabled` | bool | Control the device by a switch such as a foot pedal
| `isBrailleBackEnabled` | bool | Navigate the screen with an external Braille display
| `isVoiceAccessEnabled` | bool | iOS: Voice Control
| `fontScale` | float | Default value depends on device model. Some devices have a default font scaling of 1.1, for example |
| `displayScale` | float | Overall interface scaling ie. display density scaling. Default value may depend on device model (minSdk 24)|
| `isColorInversionEnabled` | bool | |
| `isColorBlindModeEnabled` | bool | |
| `isHighTextContrastEnabled` | bool | |
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
| `applicationId` | String | identifier for the app for which data is collected, as set in the app's Manifest. iOS: bundleId | nl.hema.mobiel |
| `defaultLanguage`| en-GB, nl-BE, nl, ... | If the country part (-BE) is not available, the value is just the language part ("nl")
| `sdkVersion` | int | 29 for Android 10. [See this list](https://source.android.com/setup/start/build-numbers)
|`manufacturer`|String|eg. `samsung`|
|`modelName`|String| May be a marketing name, but more often an internal code name. eg. `SM-G980F` for a particular variant of a Samsung Galaxy S10|


## Development

All classes and functions that are not used by implementing apps should have `internal` visibility.

Since this a library, all errors should be caught so that implementing apps don't crash. During
development, you can use the debug flavors to allow the sample app to crash in cause of an
Exception (see `handleException()`). When testing, use the release variants to make sure that
exceptions don't crash the implementing apps.

Catch Throwable; not Exception. Since Throwabl is the superclass of Exception, this will make the
lib more resilient to crashes.

For accessibility properties we want to track but could not find a property for, see [DOCUMENTATION.md](DOCUMENTATION.md)

### Setup

1. Get the API key
   from [The Api project](https://github.com/Q42/accessibility-pipeline/tree/main/api). Use this key
   in the next step.
2. Create a file called `secrets.properties` in the root of the project (not in the app folder).
   Contents:
    ```
    apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    ```
   Note that this file will be ignored by git.
3. Change the SampleApplication to construct a Q42Stats object for a real firestore collection.

### Publishing

This library is distributed using [JitPack](https://jitpack.io/#q42/q42stats.android). This makes
publishing a new version very easy:

1. In `Q42Stats.kt`, increment `DATA_MODEL_VERSION` by 1 if any changes to collected data is made.
1. Push the code for the new version to the `main` branch
1. Unit tests will be run automatically. Check [JitCI](https://jitci.com/gh/Q42/Q42Stats.Android)
   for status
1. Create a tag in the semver format: `x.x.x` without the preceding `v.`
1. On GitHub, create a release from that tag. Give it the same name; `x.x.x`
1. If everything went well the release will be visible
   on [JitPack](https://jitpack.io/#q42/q42stats.android) and the version number in the badge at the
   top of this page will update.
1. In the Sample app build.gradle, Change the
   line `jitpackImplementation 'com.github.q42:q42stats.android:x.x.x` to the latest version.

### Troubleshooting

- JitCi build failing while it can be successfully built locally

  Perhaps something is broken in JitCi. JitPack can also be used for building and might be more
  stable. To disable JitCi, select "Stop Building"
  in [JitCi's](https://jitci.com/gh/Q42/Q42Stats.Android) settings page You will lose thebuild
  status indicators in GitHub, however
