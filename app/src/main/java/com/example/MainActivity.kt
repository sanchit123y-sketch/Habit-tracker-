package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notifications.StudyNotificationHelper
import com.example.ui.AiTeacherViewModel
import com.example.ui.StudyViewModel
import com.example.ui.components.AddTaskSheet
import com.example.ui.components.StudyLeafLogoMark
import com.example.ui.screens.AiTeacherScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.StudyLeafTheme
import kotlinx.coroutines.launch

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_TASKS = "tasks"
const val ROUTE_TIMER = "timer"
const val ROUTE_PROGRESS = "progress"
const val ROUTE_AI_TEACHER = "ai_teacher"

private enum class StudyDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(
        route = ROUTE_DASHBOARD,
        labelRes = R.string.nav_dashboard,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TASKS(
        route = ROUTE_TASKS,
        labelRes = R.string.nav_tasks,
        selectedIcon = Icons.Rounded.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ),
    TIMER(
        route = ROUTE_TIMER,
        labelRes = R.string.nav_timer,
        selectedIcon = Icons.Rounded.Timer,
        unselectedIcon = Icons.Outlined.Timer
    ),
    PROGRESS(
        route = ROUTE_PROGRESS,
        labelRes = R.string.nav_progress,
        selectedIcon = Icons.Rounded.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    AI_TEACHER(
        route = ROUTE_AI_TEACHER,
        labelRes = R.string.nav_ai_teacher,
        selectedIcon = Icons.Rounded.School,
        unselectedIcon = Icons.Outlined.School
    )
}

class MainActivity : ComponentActivity() {
    private val deepLinkTabState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        StudyNotificationHelper.ensureChannel(this)
        deepLinkTabState.value = intent?.getStringExtra(StudyNotificationHelper.EXTRA_OPEN_TAB)

        setContent {
            StudyLeafTheme {
                StudyLeafApp(
                    requestedTab = deepLinkTabState.value,
                    onDeepLinkHandled = { deepLinkTabState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tab = intent.getStringExtra(StudyNotificationHelper.EXTRA_OPEN_TAB)
        if (tab != null) {
            deepLinkTabState.value = tab
        }
    }
}

@Composable
fun StudyLeafApp(
    requestedTab: String? = null,
    onDeepLinkHandled: () -> Unit = {},
    viewModel: StudyViewModel = viewModel(),
    aiTeacherViewModel: AiTeacherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aiTeacherUiState by aiTeacherViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    var showAddTaskSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(requestedTab) {
        if (requestedTab != null) {
            navigateTo(requestedTab)
            onDeepLinkHandled()
        }
    }

    val showFab = currentRoute == ROUTE_DASHBOARD || currentRoute == ROUTE_TASKS

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (showFab) {
                    FloatingActionButton(
                        onClick = { showAddTaskSheet = true },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("add_task_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add_task_cta)
                        )
                    }
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        StudyDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets(0))
                    ) {
                        Spacer(modifier = Modifier.size(16.dp))
                        StudyLeafLogoMark(size = 42.dp)
                        Spacer(modifier = Modifier.size(20.dp))
                        StudyDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_DASHBOARD
                    ) {
                        composable(ROUTE_DASHBOARD) {
                            DashboardScreen(
                                uiState = uiState,
                                onToggleTask = viewModel::toggleTaskCompletion,
                                onDeleteTask = { id ->
                                    viewModel.deleteTask(id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Task removed")
                                    }
                                },
                                onOpenAddTask = { showAddTaskSheet = true },
                                onNavigateToTasks = { navigateTo(ROUTE_TASKS) },
                                onNavigateToTimer = { navigateTo(ROUTE_TIMER) },
                                onNavigateToAiTeacher = { navigateTo(ROUTE_AI_TEACHER) },
                                onQuickAdd15Min = {
                                    viewModel.logQuickStudyMinutes(15)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Logged +15 study minutes")
                                    }
                                },
                                onToggleReminder = viewModel::setReminderEnabled,
                                onSetReminderTime = { h, m -> viewModel.setReminderTime(h, m) },
                                onToggleSkipIfGoalMet = viewModel::setSkipIfGoalMet,
                                onSendTestNotification = viewModel::sendTestReminderNotification,
                                onShowMessage = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }

                        composable(ROUTE_TASKS) {
                            TasksScreen(
                                uiState = uiState,
                                onSelectTab = viewModel::selectTaskTab,
                                onSelectSubjectFilter = viewModel::selectSubjectFilter,
                                onToggleTask = viewModel::toggleTaskCompletion,
                                onDeleteTask = { id ->
                                    viewModel.deleteTask(id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Task removed")
                                    }
                                },
                                onOpenAddTask = { showAddTaskSheet = true }
                            )
                        }

                        composable(ROUTE_TIMER) {
                            TimerScreen(
                                uiState = uiState,
                                onSelectMode = viewModel::selectTimerMode,
                                onSelectSubject = viewModel::selectTimerSubject,
                                onStart = viewModel::startTimer,
                                onPause = viewModel::pauseTimer,
                                onReset = viewModel::resetTimer,
                                onQuickAdd15Min = {
                                    viewModel.logQuickStudyMinutes(15)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Logged +15 study minutes")
                                    }
                                },
                                onToggleReminder = viewModel::setReminderEnabled,
                                onSetReminderTime = { h, m -> viewModel.setReminderTime(h, m) },
                                onToggleSkipIfGoalMet = viewModel::setSkipIfGoalMet,
                                onSendTestNotification = viewModel::sendTestReminderNotification,
                                onShowMessage = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }

                        composable(ROUTE_PROGRESS) {
                            ProgressScreen(uiState = uiState)
                        }

                        composable(ROUTE_AI_TEACHER) {
                            AiTeacherScreen(
                                uiState = aiTeacherUiState,
                                onSelectTab = aiTeacherViewModel::selectTab,
                                onSelectClassGrade = aiTeacherViewModel::selectClassGrade,
                                onSelectSubject = aiTeacherViewModel::selectSubject,
                                onUpdateChapter = aiTeacherViewModel::updateChapter,
                                onSelectLanguage = aiTeacherViewModel::selectLanguage,
                                onChatInputChange = aiTeacherViewModel::updateChatInput,
                                onSendChatQuestion = aiTeacherViewModel::sendChatMessage,
                                onClearChat = aiTeacherViewModel::clearChat,
                                onHomeworkInputChange = aiTeacherViewModel::updateHomeworkInput,
                                onExplainHomework = aiTeacherViewModel::explainHomework,
                                onQuizTopicChange = aiTeacherViewModel::updateQuizTopic,
                                onGenerateQuiz = { aiTeacherViewModel.generateQuiz() },
                                onSelectQuizOption = aiTeacherViewModel::selectQuizOption,
                                onCheckQuizAnswer = aiTeacherViewModel::checkCurrentQuizAnswer,
                                onNextQuizQuestion = aiTeacherViewModel::nextQuizQuestion,
                                onResetQuiz = aiTeacherViewModel::resetQuiz,
                                onSelectLearningTool = aiTeacherViewModel::selectLearningTool,
                                onLearningQueryChange = aiTeacherViewModel::updateLearningQuery,
                                onRunLearningTool = aiTeacherViewModel::runLearningTool
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskSheet) {
        AddTaskSheet(
            onDismiss = { showAddTaskSheet = false },
            onSaveTask = { title, notes, subject, forToday ->
                viewModel.addTask(title, notes, subject, forToday)
                scope.launch {
                    snackbarHostState.showSnackbar("Study task saved")
                }
            }
        )
    }
}
