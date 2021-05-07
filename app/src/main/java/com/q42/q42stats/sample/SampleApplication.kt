package com.q42.q42stats.sample

import android.app.Application
import com.q42.q42stats.library.Q42Stats

@Suppress("unused") // referenced from manifest only
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Q42Stats().run(this.applicationContext)
    }
}