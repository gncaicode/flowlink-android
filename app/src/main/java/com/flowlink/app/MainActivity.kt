package com.flowlink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flowlink.app.navigation.AppNavigation
import com.flowlink.app.ui.theme.FlowLinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowLinkTheme {
                AppNavigation()
            }
        }
    }
}
