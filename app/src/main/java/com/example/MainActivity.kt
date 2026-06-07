package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.StudyMateAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainActivityViewModel

class MainActivity : ComponentActivity() {
  private val viewModel by lazy { MainActivityViewModel(application) }

  private val requestPermissionLauncher = registerForActivityResult(
    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
  ) { _ ->
    // Handled permission response
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Ask for POST_NOTIFICATIONS permission on Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (androidx.core.content.ContextCompat.checkSelfPermission(
          this,
          android.Manifest.permission.POST_NOTIFICATIONS
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    // Trigger timetable reminder background alarm scheduler immediately
    TimetableReceiver.scheduleNextAlarm(this)

    setContent {
      val isDark by viewModel.isDarkMode.collectAsState()
      MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
        StudyMateAppContent(viewModel = viewModel)
      }
    }
  }
}

