# Documentation

## Settings.Secure
Many accessibity settings can be queried using constants from [Settings.secure](https://developer.android.com/reference/android/provider/Settings.Secure).

Our AccessiblityCollector has a convenience method for it: `getSystemIntAsBool`


## Attempts at reading accessibility properties

### Screen magnification
We would like to know whether the screen magnifier is enabled.

On Google Pixel 4A Android 13:
- the list of enabled accessibility services had no relevant entry
- Api 33 has `MagnificationController`, but I could not find a way to get a reference to it: [docs](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.MagnificationController)
- System property `Settings.Secure.getInt( context.getContentResolver(), "accessibility_display_magnification_enabled" )
` returned 0 while the magnifier was enabled https://stackoverflow.com/a/48032296/923557

### Bold font
We would like to know whether the font is set to bold or not.

Using `context.resources.configuration.fontWeightAdjustment`.

Tests:
- Success: Pixel 4A Android 13: 0 when bold text is off; 300 when on
- Failed:  Samsung S22+ Android 12: 0 at all times
- Failed:  Samsung S10e Android 12: 0 at all times  
