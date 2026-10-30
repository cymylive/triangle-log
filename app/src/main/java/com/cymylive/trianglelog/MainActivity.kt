package com.cymylive.trianglelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cymylive.trianglelog.ui.AppRoot
import com.cymylive.trianglelog.ui.TriangleLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TriangleLogTheme {
                AppRoot()
            }
        }
    }
}
