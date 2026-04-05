package top.alan.memorush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import top.alan.memorush.common.GameSelectionScreen
import top.alan.memorush.game.GridMemoryGameScreen
import top.alan.memorush.game.MissingItemsGameScreen
import top.alan.memorush.game.NBackGameScreen
import top.alan.memorush.game.SimonSaysGameScreen
import top.alan.memorush.game.StroopTaskGameScreen
import top.alan.memorush.model.GameType
import top.alan.memorush.ui.theme.MemoRushTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoRushTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "game_selection",
        modifier = modifier
    ) {
        composable("game_selection") {
            GameSelectionScreen(
                onGameSelected = { gameType ->
                    navController.navigate("game/${gameType.name}")
                }
            )
        }

        composable("game/{gameType}") { backStackEntry ->
            val gameTypeName = backStackEntry.arguments?.getString("gameType")
            val gameType = gameTypeName?.let { 
                try {
                    GameType.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            
            when (gameType) {
                GameType.GRID_MEMORY -> {
                    GridMemoryGameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                GameType.SIMON_SAYS -> {
                    SimonSaysGameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                GameType.N_BACK -> {
                    NBackGameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                GameType.MISSING_ITEMS -> {
                    MissingItemsGameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                GameType.STROOP_TASK -> {
                    StroopTaskGameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                else -> {
                    GamePlaceholderScreen(
                        gameType = gameType,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun GamePlaceholderScreen(
    gameType: GameType?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = gameType?.displayName ?: "未知游戏",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = gameType?.description ?: "",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "游戏开发中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onBack) {
            Text("返回选择")
        }
    }
}
