package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.*
import com.example.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun SubikshaApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("home") }
    var screenStack by remember { mutableStateOf(listOf("home")) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showQuickActionDialog by remember { mutableStateOf(false) }

    fun navigateTo(screen: String) {
        if (screen != currentScreen) {
            screenStack = screenStack + screen
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            val newStack = screenStack.dropLast(1)
            screenStack = newStack
            currentScreen = newStack.last()
        } else if (currentScreen != "home") {
            currentScreen = "home"
            screenStack = listOf("home")
        }
    }

    BackHandler(enabled = currentScreen != "home" || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            navigateBack()
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onKeySaved = { /* updated in SharedPreferences */ }
        )
    }

    if (showQuickActionDialog) {
        QuickActionDialog(
            onDismiss = { showQuickActionDialog = false },
            onNavigate = { screen -> navigateTo(screen) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SubikshaDrawerContent(
                currentScreen = currentScreen,
                onNavigate = { screen -> navigateTo(screen) },
                onOpenApiKeyDialog = { showApiKeyDialog = true },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                // Show bottom bar on primary tabs
                if (currentScreen in listOf("home", "explore", "profile", "settings")) {
                    SubikshaBottomNavigation(
                        currentScreen = currentScreen,
                        onNavigate = { screen -> navigateTo(screen) },
                        onPlusClick = { showQuickActionDialog = true }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            when (currentScreen) {
                "home" -> {
                    HomeScreenWrapper(
                        onNavigate = { screen -> navigateTo(screen) },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onKeyClick = { showApiKeyDialog = true },
                        onProfileClick = { navigateTo("profile") }
                    )
                }
                "gemini_chat" -> {
                    GeminiChatScreen(
                        onBackClick = { navigateBack() }
                    )
                }
                "image_to_text" -> {
                    ImageToTextScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onAskGemini = { _ ->
                            navigateTo("gemini_chat")
                        }
                    )
                }
                "voice_chat" -> {
                    VoiceChatScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onAskGemini = { _ ->
                            navigateTo("gemini_chat")
                        }
                    )
                }
                "spoken_english" -> {
                    SpokenEnglishScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onAskGemini = { _ ->
                            navigateTo("gemini_chat")
                        }
                    )
                }
                "mental_maths" -> {
                    MentalMathsScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onAskGemini = { _ ->
                            navigateTo("gemini_chat")
                        }
                    )
                }
                "more_features", "explore" -> {
                    ExploreScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onAskGemini = { _ ->
                            navigateTo("gemini_chat")
                        }
                    )
                }
                "profile" -> {
                    ProfileScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                "settings" -> {
                    SettingsScreen(
                        onBackClick = { navigateBack() },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )
                }
                else -> {
                    HomeScreenWrapper(
                        onNavigate = { screen -> navigateTo(screen) },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onKeyClick = { showApiKeyDialog = true },
                        onProfileClick = { navigateTo("profile") }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenWrapper(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onKeyClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    HomeScreen(
        onNavigate = onNavigate,
        onMenuClick = onMenuClick,
        onProfileClick = onProfileClick
    )
}
