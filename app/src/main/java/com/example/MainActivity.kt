package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.GameDatabase
import com.example.data.repository.GameRepository
import com.example.model.GameMode
import com.example.ui.screens.ArsenalScreen
import com.example.ui.screens.CampaignHubScreen
import com.example.ui.screens.CombatScreen
import com.example.ui.screens.SquadLobbyScreen
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CarbonPlate
import com.example.ui.theme.DeadzoneProtocolTheme
import com.example.ui.theme.ObsidianAsphalt
import com.example.ui.theme.TacticalMuted
import com.example.ui.theme.ToxicAcidLime
import com.example.ui.viewmodel.GameViewModel

object Routes {
    const val CAMPAIGN = "campaign"
    const val SQUAD_LOBBY = "squad_lobby"
    const val ARSENAL = "arsenal"
    const val COMBAT = "combat"
}

private data class HubNavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeadzoneProtocolTheme {
                DeadzoneApp()
            }
        }
    }
}

@Composable
fun DeadzoneApp() {
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(
        factory = remember(context) {
            val db = GameDatabase.getInstance(context)
            GameViewModel.factory(GameRepository(db.gameDao()))
        }
    )

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val stash by viewModel.stash.collectAsStateWithLifecycle()
    val logs by viewModel.deploymentLogs.collectAsStateWithLifecycle()
    val banner by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val combatHud by viewModel.combatHud.collectAsStateWithLifecycle()
    val isCombatPaused by viewModel.isCombatPaused.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.CAMPAIGN
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(banner) {
        val msg = banner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    val hubTabs = remember {
        listOf(
            HubNavTab(Routes.CAMPAIGN, R.string.nav_campaign, Icons.Filled.MilitaryTech, Icons.Outlined.MilitaryTech, "tab_campaign"),
            HubNavTab(Routes.SQUAD_LOBBY, R.string.nav_coop_squad, Icons.Filled.Groups, Icons.Outlined.Groups, "tab_squad_lobby"),
            HubNavTab(Routes.ARSENAL, R.string.nav_arsenal, Icons.Filled.Shield, Icons.Outlined.Shield, "tab_arsenal")
        )
    }

    val showSideRail = currentRoute != Routes.COMBAT

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianAsphalt,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showSideRail) innerPadding else PaddingValues())
        ) {
            if (showSideRail) {
                NavigationRail(
                    containerColor = CarbonPlate,
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Spacer(Modifier.weight(1f))
                    hubTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.labelRes),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = ToxicAcidLime,
                                selectedTextColor = ToxicAcidLime,
                                unselectedIconColor = TacticalMuted,
                                unselectedTextColor = TacticalMuted,
                                indicatorColor = BunkerSurface
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (showSideRail) Modifier.windowInsetsPadding(WindowInsets.safeDrawing) else Modifier)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.CAMPAIGN
                ) {
                    composable(Routes.CAMPAIGN) {
                        CampaignHubScreen(
                            profile = profile,
                            onSelectChapter = viewModel::selectChapter,
                            onDeployChapter = { chapter ->
                                viewModel.selectMode(GameMode.SINGLE_PLAYER)
                                viewModel.startDeployment(GameMode.SINGLE_PLAYER, chapter)
                                navController.navigate(Routes.COMBAT)
                            }
                        )
                    }

                    composable(Routes.SQUAD_LOBBY) {
                        SquadLobbyScreen(
                            profile = profile,
                            stash = stash,
                            logs = logs,
                            onUpdateCallsign = viewModel::updateCallsign,
                            onSelectDistrict = viewModel::selectDistrict,
                            onNewSquadCode = viewModel::generateNewSquadRoom,
                            onJoinSquadCode = viewModel::joinSquadRoom,
                            onCookMeal = viewModel::cookMeal,
                            onEatMeal = viewModel::eatMeal,
                            onDeployClick = {
                                viewModel.selectMode(GameMode.COOP_SQUAD)
                                viewModel.startDeployment(GameMode.COOP_SQUAD, null)
                                navController.navigate(Routes.COMBAT)
                            }
                        )
                    }

                    composable(Routes.ARSENAL) {
                        ArsenalScreen(
                            profile = profile,
                            onSelectOperator = viewModel::selectOperator,
                            onSelectWeapon = viewModel::selectWeapon
                        )
                    }

                    composable(Routes.COMBAT) {
                        CombatScreen(
                            hud = combatHud,
                            isPaused = isCombatPaused,
                            snapshotProvider = viewModel::currentRenderSnapshot,
                            onMoveInput = viewModel::setMoveInput,
                            onLookDelta = viewModel::addLookDelta,
                            onJump = viewModel::jump,
                            onSetAds = viewModel::setAds,
                            onSetFiring = viewModel::setFiring,
                            onSlide = viewModel::triggerSlide,
                            onSwapWeapon = viewModel::swapWeapon,
                            onReload = viewModel::reloadWeapon,
                            onEatRation = viewModel::quickEatRation,
                            onScavenge = viewModel::scavengeNearby,
                            onSquadPing = viewModel::sendSquadPing,
                            onTogglePause = viewModel::setCombatPaused,
                            onExtract = viewModel::extractFromCombat,
                            onExitToLobby = {
                                viewModel.stopDeployment()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
