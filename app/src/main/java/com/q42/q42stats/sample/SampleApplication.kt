package com.q42.q42stats.sample

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.annotation.RequiresApi
import com.q42.q42stats.library.Q42Stats
import com.q42.q42stats.library.Q42StatsConfig
import com.q42.q42stats.library.Q42StatsLogLevel


@Suppress("unused") // SampleApplication is referenced from manifest only
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setStrictMode()
        }
        Q42Stats.logLevel = Q42StatsLogLevel.Debug
        Q42Stats(
            Q42StatsConfig(
                firestoreCollectionId = "testCollection",
                apiKey = BuildConfig.API_KEY,
                // wait at least 7.5 days between data collections. the extra .5 is for time-of-day randomization
                minimumSubmitIntervalSeconds = (60 * 60 * 24 * 7.5).toLong()
            )
        ).runAsync(this.applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setStrictMode() {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectAll() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectActivityLeaks()
                .detectFileUriExposure()
                //.detectUntaggedSockets() exclude okhttp bug
                .detectContentUriWithoutPermission()
                .detectLeakedRegistrationObjects()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                //.detectNonSdkApiUsage() // exclude triggered by AppCompatActivity
                .detectCleartextNetwork()
                .penaltyLog()
                .build()
        )
    }
}
