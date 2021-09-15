package com.q42.q42stats.sample

import android.app.Application
import com.q42.q42stats.library.Q42Stats
import com.q42.q42stats.library.Q42StatsConfig

@Suppress("unused") // SampleApplication is referenced from manifest only
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