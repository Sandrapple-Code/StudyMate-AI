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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDark by viewModel.isDarkMode.collectAsState()
      MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
        StudyMateAppContent(viewModel = viewModel)
      }
    }
  }
}

