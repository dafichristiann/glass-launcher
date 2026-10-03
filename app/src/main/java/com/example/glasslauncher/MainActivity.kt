package com.example.glasslauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.glasslauncher.ui.home.HomeScreen
import com.example.glasslauncher.ui.home.HomeViewModel
import com.example.glasslauncher.ui.theme.GlassLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single HOME activity (PRD section 5). `singleTask` + `stateNotNeeded` keep Home warm so
 * returning from another app is under 300ms (PRD section 12).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GlassLauncherTheme {
                LauncherRoot()
            }
        }
    }
}

@Composable
private fun LauncherRoot(viewModel: HomeViewModel = hiltViewModel()) {
    HomeScreen(viewModel = viewModel)
}
