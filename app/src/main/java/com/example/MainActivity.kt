package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppThemeMode
import com.example.ui.components.NotchedBottomBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.*
import com.example.ui.theme.CausaSuiTheme
import com.example.ui.theme.LocalCausaSuiColors
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val viewModel: CausaSuiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            var onboardingTheme by remember { mutableStateOf(AppThemeMode.BEIGE) }

            val activeTheme = if (state.profile.onboardingCompleted) state.themeMode else onboardingTheme
            val isDark = activeTheme == AppThemeMode.DARK

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
            }

            val reduceMotion = state.profile.reduceMotion || viewModel.feedback.isSystemReducedMotion()

            CausaSuiTheme(darkTheme = isDark) {
                val colors = LocalCausaSuiColors.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(state.toastMessage) {
                    val msg = state.toastMessage
                    if (msg != null) {
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearToast()
                    }
                }

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize().background(colors.background))
                } else if (!state.profile.onboardingCompleted) {
                    OnboardingScreen(
                        currentTheme = onboardingTheme,
                        onSelectTheme = { onboardingTheme = it },
                        onComplete = { profile -> viewModel.completeOnboarding(profile) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background)
                    ) {
                        Scaffold(
                            containerColor = colors.background,
                            snackbarHost = {
                                SnackbarHost(
                                    hostState = snackbarHostState,
                                    modifier = Modifier.navigationBarsPadding()
                                )
                            },
                            contentWindowInsets = WindowInsets.safeDrawing
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = innerPadding.calculateTopPadding())
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    val subtitle = when (state.activeTab) {
                                        MainTab.ACCUEIL -> "Salut ${state.profile.pseudo}"
                                        MainTab.PROGRAMME -> "Ma semaine · ${state.profile.daysPerWeek} jours"
                                        MainTab.NUTRITION -> "Aujourd'hui"
                                        MainTab.PROGRES -> "Semaine · Mois"
                                        MainTab.MOI -> "Badge Ranked"
                                    }
                                    TopHeaderBar(
                                        subtitle = subtitle,
                                        onOpenSettings = { viewModel.openOverlay(AppOverlay.Settings) },
                                        onOpenBackup = { viewModel.openOverlay(AppOverlay.Backup) },
                                        onToggleTheme = { viewModel.toggleTheme() }
                                    )

                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        when (state.activeTab) {
                                            MainTab.ACCUEIL -> HomeScreen(
                                                state = state,
                                                reduceMotion = reduceMotion,
                                                onSetLocation = viewModel::setWorkoutLocation,
                                                onLaunchWorkout = viewModel::startWorkout,
                                                onValidateRestDay = viewModel::validateRestDay,
                                                onAddWater = viewModel::addWater,
                                                onLogWeight = viewModel::logNewWeight,
                                                onNavigateTab = viewModel::selectTab
                                            )
                                            MainTab.PROGRAMME -> ProgramScreen(
                                                state = state,
                                                onSetDaysPerWeek = { d ->
                                                    viewModel.updateProfile(state.profile.copy(daysPerWeek = d))
                                                },
                                                onSetThreeDayPpl = viewModel::setThreeDayPpl,
                                                onSetLocation = viewModel::setWorkoutLocation,
                                                onSetDifficulty = viewModel::setDifficulty,
                                                onLaunchWorkout = viewModel::startWorkout,
                                            )
                                            MainTab.NUTRITION -> NutritionScreen(
                                                state = state,
                                                onAddWater = viewModel::addWater,
                                                onUndoWater = viewModel::undoWater,
                                                onAddFoodFromCatalog = viewModel::addFoodFromCatalog,
                                                onAddDirectCalories = viewModel::addDirectCalories,
                                                onDeleteFood = viewModel::deleteFoodLog,
                                            )
                                            MainTab.PROGRES -> ProgressScreen(
                                                state = state,
                                                onAddMeasurement = viewModel::addMeasurement,
                                                onSaveNote = viewModel::saveDailyNote,
                                            )
                                            MainTab.MOI -> MeScreen(
                                                state = state,
                                                reduceMotion = reduceMotion,
                                                onOpenRankedQuestionnaire = { viewModel.openOverlay(AppOverlay.RankedQuestionnaire) },
                                                onOpenAvatarBuilder = { viewModel.openOverlay(AppOverlay.AvatarBuilder) },
                                                onUpdateCharacterColors = viewModel::updateCharacterColors,
                                                onToggleTheme = viewModel::toggleTheme,
                                            )
                                        }
                                    }
                                }

                                // Barre flottante à encoche courbe + pastille glissante
                                NotchedBottomBar(
                                    activeTab = state.activeTab,
                                    reduceMotion = reduceMotion,
                                    onSelectTab = viewModel::selectTab,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }

                        // Écrans secondaires et modales
                        when (val ov = state.overlay) {
                            AppOverlay.None -> {}
                            is AppOverlay.WorkoutPlayer -> WorkoutSessionScreen(
                                state = state,
                                session = ov.session,
                                reduceMotion = reduceMotion,
                                onAdvance = viewModel::advanceWorkout,
                                onUpgradeHomeVariant = viewModel::upgradeHomeVariant,
                                onUpdateCalories = viewModel::updateSummaryCalories,
                                onValidateSession = viewModel::validateAndSaveWorkout,
                                onQuitConfirmed = viewModel::closeOverlay,
                            )
                            AppOverlay.RankedQuestionnaire -> RankedQuestionnaireScreen(
                                state = state,
                                onSubmit = viewModel::submitRanked,
                                onClose = viewModel::closeOverlay,
                            )
                            AppOverlay.Settings -> SettingsScreen(
                                state = state,
                                onSaveProfile = { updated -> viewModel.updateProfile(updated, logWeightIfChanged = true) },
                                onToggleTheme = viewModel::toggleTheme,
                                onClose = viewModel::closeOverlay,
                            )
                            AppOverlay.Backup -> BackupModal(
                                onExportJson = viewModel::exportBackupJson,
                                onImportJson = viewModel::importBackupJson,
                                onToast = viewModel::showToast,
                                onDismiss = viewModel::closeOverlay,
                            )
                            AppOverlay.AvatarBuilder -> AvatarBuilderScreen(
                                initial = state.avatarConfig,
                                onSave = viewModel::saveAvatar,
                                onClose = viewModel::closeOverlay,
                            )
                        }
                    }
                }
            }
        }
    }
}
