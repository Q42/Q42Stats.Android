package com.q42.q42stats.sample.ui.main

import androidx.lifecycle.ViewModel
import com.q42.q42stats.library.Q42Stats

class MainViewModel : ViewModel() {
    val message = "Hello from ${Q42Stats().name}"
}