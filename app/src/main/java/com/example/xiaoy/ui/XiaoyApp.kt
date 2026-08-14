package com.example.xiaoy.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.Celebration
import com.example.xiaoy.ui.components.LocalSnackbar
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.screens.DetailScreen
import com.example.xiaoy.ui.screens.EditScreen
import com.example.xiaoy.ui.screens.GalleryScreen
import com.example.xiaoy.ui.screens.HomeScreen
import com.example.xiaoy.ui.screens.ImageViewScreen
import com.example.xiaoy.ui.screens.InsightsScreen
import com.example.xiaoy.ui.screens.OnboardingScreen
import com.example.xiaoy.ui.screens.PlanScreen
import com.example.xiaoy.ui.screens.RecordsScreen
import com.example.xiaoy.ui.screens.ReportScreen
import com.example.xiaoy.ui.screens.SettingsScreen
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.XiaoYTheme
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.Paper

private data class TabItem(val route: Route, val label: String, val filled: ImageVector, val outlined: ImageVector)

private val tabs = listOf(
    TabItem(Route.Home, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    TabItem(Route.Records, "记录", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    TabItem(Route.Plan, "计划", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    TabItem(Route.Insights, "洞察", Icons.Filled.Insights, Icons.Outlined.Insights),
    TabItem(Route.Settings, "我的", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun XiaoyApp() {
    val context = LocalContext.current
    val appState = remember { AppState(context) }
    val data by appState.data.collectAsState()
    val darkTheme = when (data.themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    XiaoYTheme(darkTheme = darkTheme) {
        val snackbar = remember { SnackbarHostState() }

    val backStack = remember { mutableStateListOf<Route>() }
    if (backStack.isEmpty()) {
        val first = if (appState.data.value.profile == null) Route.Onboarding else Route.Home
        backStack.add(first)
    }
    val current = backStack.lastOrNull() ?: Route.Home

    val navigate: (Route) -> Unit = { backStack.add(it) }
    val popBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    val switchTab: (Route) -> Unit = { backStack.clear(); backStack.add(it) }

    BackHandler(enabled = backStack.size > 1) { popBack() }

    CompositionLocalProvider(LocalSnackbar provides snackbar) {
        Scaffold(
            containerColor = Cream,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (Route.isBottomTab(current)) {
                    BottomNavBar(current, onSelect = switchTab)
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (current) {
                    is Route.Home -> HomeScreen(appState, nav = navigate)
                    is Route.Records -> RecordsScreen(appState, nav = navigate, back = popBack)
                    is Route.Plan -> PlanScreen(appState, nav = navigate)
                    is Route.Insights -> InsightsScreen(appState, nav = navigate)
                    is Route.Gallery -> GalleryScreen(appState, nav = navigate, back = popBack)
                    is Route.Settings -> SettingsScreen(appState, nav = navigate)
                    is Route.Onboarding -> OnboardingScreen(appState, onDone = { switchTab(Route.Home) })
                    is Route.Detail -> DetailScreen(appState, id = current.id, nav = navigate, back = popBack)
                    is Route.Edit -> EditScreen(appState, id = current.id, presetType = current.presetType, back = popBack)
                    is Route.ImageView -> ImageViewScreen(appState, current.recordId, current.index, back = popBack)
                    is Route.Report -> ReportScreen(appState, current.type, back = popBack)
                }
            }
        }

        // 完成亲子目标后的成就反馈
        val celebration by appState.celebration.collectAsState()
        celebration?.let { c ->
            CelebrationOverlay(c) { appState.consumeCelebration() }
        }
    }
    }
}

@Composable
private fun BottomNavBar(current: Route, onSelect: (Route) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper)
            .navigationBarsPadding()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val selected = Route.tabIndex(tab.route) == Route.tabIndex(current)
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(tab.route) }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (selected) tab.filled else tab.outlined, null,
                    tint = if (selected) Apricot else InkFaint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(tab.label, style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Apricot else InkFaint,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun CelebrationOverlay(c: Celebration, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Paper)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(LeafGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Spa, null, tint = LeafGreen, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(c.title, style = MaterialTheme.typography.titleLarge, color = Ink,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(c.message, style = MaterialTheme.typography.bodyLarge, color = InkSoft,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text("成长树已有 ${c.leafCount} 片叶子", style = MaterialTheme.typography.labelMedium,
                color = Apricot)
            Spacer(Modifier.height(20.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                    .background(Apricot).padding(vertical = 6.dp)
            ) {
                Text("收下这份鼓励", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
