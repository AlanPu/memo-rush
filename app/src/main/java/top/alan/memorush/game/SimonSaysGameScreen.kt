package top.alan.memorush.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.SimonColor
import top.alan.memorush.model.SimonGameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimonSaysGameScreen(
    onBack: () -> Unit,
    viewModel: SimonSaysGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Simon Says",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (gameState.gamePhase) {
                GamePhase.GAME_OVER -> {
                    GameOverContent(
                        gameState = gameState,
                        onRetry = { viewModel.resetGame() },
                        onBack = onBack
                    )
                }
                else -> {
                    GameContent(
                        gameState = gameState,
                        onStartGame = { viewModel.startGame() },
                        onColorClick = { color -> viewModel.selectColor(color) },
                        onNextLevel = { viewModel.nextLevel() },
                        onUpdateColorCount = { count -> viewModel.updateColorCount(count) },
                        onResetGame = { viewModel.resetGame() },
                        onUpdateReverseMode = { reverse -> viewModel.updateReverseMode(reverse) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameContent(
    gameState: SimonGameState,
    onStartGame: () -> Unit,
    onColorClick: (SimonColor) -> Unit,
    onNextLevel: () -> Unit,
    onUpdateColorCount: (Int) -> Unit,
    onResetGame: () -> Unit,
    onUpdateReverseMode: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameInfoSection(gameState = gameState)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GameStatusMessage(gamePhase = gameState.gamePhase, reverseMode = gameState.reverseMode)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ColorGrid(
                colorCount = gameState.colorCount,
                currentHighlightColor = gameState.currentHighlightColor,
                enabled = gameState.gamePhase == GamePhase.USER_INPUT,
                onColorClick = onColorClick,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GameActionButtons(
            gamePhase = gameState.gamePhase,
            colorCount = gameState.colorCount,
            reverseMode = gameState.reverseMode,
            onStartGame = onStartGame,
            onNextLevel = onNextLevel,
            onUpdateColorCount = onUpdateColorCount,
            onResetGame = onResetGame,
            onUpdateReverseMode = onUpdateReverseMode
        )
    }
}

@Composable
private fun GameInfoSection(gameState: SimonGameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(
                label = "关卡",
                value = "Level ${gameState.currentLevel}"
            )
            
            InfoItem(
                label = "序列长度",
                value = "${gameState.sequenceLength} 个颜色"
            )
            
            InfoItem(
                label = "当前进度",
                value = "${gameState.currentInputIndex}/${gameState.sequenceLength}"
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun GameStatusMessage(gamePhase: GamePhase, reverseMode: Boolean) {
    val (message, subMessage) = when (gamePhase) {
        GamePhase.IDLE -> "准备开始" to "点击下方按钮开始游戏"
        GamePhase.SHOWING_SEQUENCE -> "观察记忆" to if (reverseMode) "请记住颜色序列（需要倒序输入）" else "请记住颜色序列"
        GamePhase.USER_INPUT -> "开始选择" to if (reverseMode) "按相反顺序点击颜色" else "按相同顺序点击颜色"
        GamePhase.LEVEL_COMPLETE -> "恭喜过关！" to "点击继续进入下一关"
        GamePhase.GAME_OVER -> "游戏结束" to "再接再厉，继续挑战！"
    }
    
    val backgroundColor = when (gamePhase) {
        GamePhase.IDLE -> MaterialTheme.colorScheme.surfaceVariant
        GamePhase.SHOWING_SEQUENCE -> MaterialTheme.colorScheme.tertiaryContainer
        GamePhase.USER_INPUT -> MaterialTheme.colorScheme.primaryContainer
        GamePhase.LEVEL_COMPLETE -> Color(0xFF4CAF50)
        GamePhase.GAME_OVER -> MaterialTheme.colorScheme.errorContainer
    }
    
    val textColor = when (gamePhase) {
        GamePhase.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
        GamePhase.SHOWING_SEQUENCE -> MaterialTheme.colorScheme.onTertiaryContainer
        GamePhase.USER_INPUT -> MaterialTheme.colorScheme.onPrimaryContainer
        GamePhase.LEVEL_COMPLETE -> Color.White
        GamePhase.GAME_OVER -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ColorGrid(
    colorCount: Int,
    currentHighlightColor: SimonColor?,
    enabled: Boolean,
    onColorClick: (SimonColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SimonColor.values().take(colorCount)
    val gridSize = when (colorCount) {
        4 -> 2
        6 -> 3
        9 -> 3
        else -> 2
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.chunked(gridSize).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowColors.forEach { color ->
                    ColorBlock(
                        color = color,
                        isHighlighted = currentHighlightColor == color,
                        enabled = enabled,
                        onClick = { onColorClick(color) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(gridSize - rowColors.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GameActionButtons(
    gamePhase: GamePhase,
    colorCount: Int,
    reverseMode: Boolean,
    onStartGame: () -> Unit,
    onNextLevel: () -> Unit,
    onUpdateColorCount: (Int) -> Unit,
    onResetGame: () -> Unit,
    onUpdateReverseMode: (Boolean) -> Unit
) {
    when (gamePhase) {
        GamePhase.IDLE -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCard(
                    currentColorCount = colorCount,
                    reverseMode = reverseMode,
                    onColorCountSelected = onUpdateColorCount,
                    onReverseModeChanged = onUpdateReverseMode
                )
                
                OutlinedButton(
                    onClick = onResetGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "重置游戏",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "开始游戏",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        GamePhase.LEVEL_COMPLETE -> {
            Button(
                onClick = onNextLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "下一关",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        else -> {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    currentColorCount: Int,
    reverseMode: Boolean,
    onColorCountSelected: (Int) -> Unit,
    onReverseModeChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "游戏规则",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "1. 游戏开始时，颜色块会依次闪烁\n" +
                       "2. 记住颜色闪烁的顺序\n" +
                       "3. 闪烁结束后，按相同顺序点击颜色块\n" +
                       "4. 选对所有颜色即可过关\n" +
                       "5. 选错颜色则游戏结束",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "游戏设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "颜色块数量",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorCountOption(
                    count = 4,
                    label = "4个",
                    isSelected = currentColorCount == 4,
                    onClick = { onColorCountSelected(4) }
                )
                
                ColorCountOption(
                    count = 6,
                    label = "6个",
                    isSelected = currentColorCount == 6,
                    onClick = { onColorCountSelected(6) }
                )
                
                ColorCountOption(
                    count = 9,
                    label = "9个",
                    isSelected = currentColorCount == 9,
                    onClick = { onColorCountSelected(9) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "倒序模式",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "开启后需要按相反顺序点击",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = reverseMode,
                    onCheckedChange = onReverseModeChanged
                )
            }
        }
    }
}

@Composable
private fun RowScope.ColorCountOption(
    count: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun GameOverContent(
    gameState: SimonGameState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "游戏结束",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "最终成绩",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Level ${gameState.currentLevel}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "成功记忆了 ${gameState.sequenceLength - 1} 个颜色",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "重试",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "返回游戏选择",
                fontSize = 16.sp
            )
        }
    }
}
