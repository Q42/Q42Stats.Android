# Q42Stats.Android 
[![Release](https://jitpack.io/v/Q42/Q42Stats.Android.svg)](https://jitpack.io/#Q42/Q42Stats.Android)
[![](https://jitci.com/gh/Q42/Q42Stats.Android/svg)](https://jitci.com/gh/Q42/Q42Stats.Android)


Collect stats for Q42 internal usage, shared accross multiple Android projects.

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
       implementation 'com.github.Q42:Q42Stats.Android:X.X.X' // Replace X.X.X by the latest version,
       // which is available in the Jitpack badge at the top of this page
   }
```  

## Usage

Call `Q42Stats.runAsync(Context)` from anywhere in your app. This can be safely called from the main thread since all work (both collecting statistics and sending them to the server) are done on an IO thread. 

```
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Q42Stats().runAsync(this.applicationContext)
    }
}
```
It is safe to call this function multiple times, as it will exit immediately if it is already running or when a data collection interval has not passed yet.

## Data collected

Not all fields are supported on all versions of Android. If unsupported, the corresponding value may be false, "unknown" or the key may be completely omitted.

### Accessibliity

| Key | Value | Notes |
|-|-|-|
| `isAccessibilityManagerEnabled` | bool | true when any accessibility service (eg. Talkback) is Enabled | 
| `isClosedCaptioningEnabled` | bool | Live transcription of any spoken audio |
| `isTouchExplorationEnabled` | bool | Wehether any assistive feature is enabled wher the user navigates the interface by touch. Most probably TalkbBack, or similar 
| `isTalkBackEnabled` | bool | iOS: VoiceOver
| `isVoiceAccessEnabled` | bool | iOS: Voice Control
| `fontScale` | float | 1.0 is regular scaling |
| `displayScale` | float | Overall interface scaling ie. display density scaling. 1.0 is regular scaling |

### Preferences

| Key | Value | Notes |
|-|-|-|
| `daytime`| day, twilight, night, unknown | Coarse estimation of time of day. unknown if user is not in Amsterdam TimeZone
| `isNightModeEnabled` | bool | iOS: Dark Mode

### Screen

| Key | Value | Notes |
|-|-|-|
| `screenOrientation`| portrait, landscape, unknown |

### System

| Key | Value | Notes |
|-|-|-|
| `defaultLanguage`| en, nl, ... |


