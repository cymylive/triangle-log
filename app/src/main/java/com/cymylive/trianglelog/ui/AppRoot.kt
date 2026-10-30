package com.cymylive.trianglelog.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppRoot() {
    val state: AppState = viewModel()
    var tab by remember { mutableIntStateOf(0) }

    val items = listOf(
        Triple("今日", Icons.Filled.CheckCircle, 0),
        Triple("回看", Icons.Filled.List, 1),
        Triple("目标", Icons.Filled.Star, 2),
        Triple("统计", Icons.Filled.Info, 3),
        Triple("局域网", Icons.Filled.Share, 4)
    )

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Surface1) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item.third,
                        onClick = { tab = item.third },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            unselectedIconColor = TextSub,
                            unselectedTextColor = TextSub,
                            indicatorColor = AccentDim
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> TodayScreen(state)
                1 -> TimelineScreen(state)
                2 -> GoalsScreen(state)
                3 -> StatsScreen(state)
                else -> LanScreen(state)
            }
        }
    }
}
