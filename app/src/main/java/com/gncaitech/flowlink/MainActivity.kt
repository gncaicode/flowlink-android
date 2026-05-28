package com.gncaitech.flowlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gncaitech.flowlink.navigation.AppNavigation
import com.gncaitech.flowlink.ui.theme.FlowLinkTheme

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
