# Documentation

## Settings.Secure
Many accessibity settings can be queried using constants from [Settings.secure](https://developer.android.com/reference/android/provider/Settings.Secure).

Our AccessiblityCollector has a convenience method for it: `getSystemIntAsBool`

## Attempts to read accessibility settings

- Mono audio: Only found a private system setting: `MASTER_MONO`. These settings require root access.
