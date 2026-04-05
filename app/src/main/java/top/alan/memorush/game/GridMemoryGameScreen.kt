package top.alan.memorush.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.GameState
import top.alan.memorush.model.GridCell
import top.alan.memorush.ui.theme.CardBackground
import top.alan.memorush.ui.theme.CardBorder
import top.alan.memorush.ui.theme.DarkBackground
import top.alan.memorush.ui.theme.ErrorRed
import top.alan.memorush.ui.theme.GlowCyan
import top.alan.memorush.ui.theme.GlowPink
import top.alan.memorush.ui.theme.GlowPurple
import top.alan.memorush.ui.theme.GradientEnd
import top.alan.memorush.ui.theme.GradientMiddle
import top.alan.memorush.ui.theme.GradientStart
import top.alan.memorush.ui.theme.NeonCyan
import top.alan.memorush.ui.theme.NeonGreen
import top.alan.memorush.ui.theme.NeonOrange
import top.alan.memorush.ui.theme.NeonPurple
import top.alan.memorush.ui.theme.SuccessGreen
import top.alan.memorush.ui.theme.TextMuted
import top.alan.memorush.ui.theme.TextPrimary
import top.alan.memorush.ui.theme.TextSecondary
import top.alan.memorush.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridMemoryGameScreen(
    onBack: () -> Unit,
    viewModel: GridMemoryGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "网格记忆游戏",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(
                                color = NeonCyan.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 10f
                            )
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
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
                        onCellClick = { row, col -> viewModel.selectCell(row, col) },
                        onNextLevel = { viewModel.nextLevel() },
                        onUpdateGridSize = { size -> viewModel.updateGridSize(size) },
                        onResetGame = { viewModel.resetGame() },
                        onUpdateShowDistractors = { show -> viewModel.updateShowDistractors(show) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameContent(
    gameState: GameState,
    onStartGame: () -> Unit,
    onCellClick: (Int, Int) -> Unit,
    onNextLevel: () -> Unit,
    onUpdateGridSize: (Int) -> Unit,
    onResetGame: () -> Unit,
    onUpdateShowDistractors: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameInfoSection(gameState = gameState)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GameStatusMessage(gamePhase = gameState.gamePhase)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val allCells = buildAllCells(
                gridSize = gameState.gridSize,
                flashingSequence = gameState.flashingSequence,
                userSelections = gameState.userSelections
            )
            
            GameGrid(
                gridSize = gameState.gridSize,
                cells = allCells,
                enabled = gameState.gamePhase == GamePhase.USER_INPUT,
                onCellClick = onCellClick,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GameActionButtons(
            gamePhase = gameState.gamePhase,
            gridSize = gameState.gridSize,
            showDistractors = gameState.showDistractors,
            onStartGame = onStartGame,
            onNextLevel = onNextLevel,
            onUpdateGridSize = onUpdateGridSize,
            onResetGame = onResetGame,
            onUpdateShowDistractors = onUpdateShowDistractors
        )
    }
}

@Composable
private fun GameInfoSection(gameState: GameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = NeonCyan.copy(alpha = 0.2f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
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
                label = "需要记忆",
                value = "${gameState.flashCount} 个格子"
            )
            
            InfoItem(
                label = "当前进度",
                value = "${gameState.currentSelectionIndex}/${gameState.flashCount}"
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
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )
    }
}

@Composable
private fun GameStatusMessage(gamePhase: GamePhase) {
    val (message, subMessage) = when (gamePhase) {
        GamePhase.IDLE -> "准备开始" to "点击下方按钮开始游戏"
        GamePhase.SHOWING_SEQUENCE -> "观察记忆" to "请按顺序记住红色闪烁的格子位置"
        GamePhase.USER_INPUT -> "开始选择" to "按闪烁顺序点击格子"
        GamePhase.LEVEL_COMPLETE -> "恭喜过关！" to "点击继续进入下一关"
        GamePhase.GAME_OVER -> "游戏结束" to "再接再厉，继续挑战！"
    }
    
    val backgroundColor = when (gamePhase) {
        GamePhase.IDLE -> CardBackground
        GamePhase.SHOWING_SEQUENCE -> NeonPurple.copy(alpha = 0.2f)
        GamePhase.USER_INPUT -> NeonCyan.copy(alpha = 0.2f)
        GamePhase.LEVEL_COMPLETE -> SuccessGreen.copy(alpha = 0.2f)
        GamePhase.GAME_OVER -> ErrorRed.copy(alpha = 0.2f)
    }
    
    val textColor = when (gamePhase) {
        GamePhase.IDLE -> TextSecondary
        GamePhase.SHOWING_SEQUENCE -> GlowPurple
        GamePhase.USER_INPUT -> NeonCyan
        GamePhase.LEVEL_COMPLETE -> SuccessGreen
        GamePhase.GAME_OVER -> ErrorRed
    }
    
    Card(
        shape = RoundedCornerShape(12.dp),
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
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GameActionButtons(
    gamePhase: GamePhase,
    gridSize: Int,
    showDistractors: Boolean,
    onStartGame: () -> Unit,
    onNextLevel: () -> Unit,
    onUpdateGridSize: (Int) -> Unit,
    onResetGame: () -> Unit,
    onUpdateShowDistractors: (Boolean) -> Unit
) {
    when (gamePhase) {
        GamePhase.IDLE -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCard(
                    currentGridSize = gridSize,
                    showDistractors = showDistractors,
                    onGridSizeSelected = onUpdateGridSize,
                    onShowDistractorsChanged = onUpdateShowDistractors
                )
                
                OutlinedButton(
                    onClick = onResetGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Text(
                        text = "重置游戏",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                CyberButton(
                    onClick = onStartGame,
                    text = "开始游戏"
                )
            }
        }
        GamePhase.LEVEL_COMPLETE -> {
            CyberButton(
                onClick = onNextLevel,
                text = "下一关"
            )
        }
        else -> {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    currentGridSize: Int,
    showDistractors: Boolean,
    onGridSizeSelected: (Int) -> Unit,
    onShowDistractorsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlowPurple.copy(alpha = 0.3f), GlowPink.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
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
                color = GlowPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "1. 游戏开始时，部分格子会依次闪烁红色\n" +
                       "2. 按顺序记住这些闪烁格子的位置\n" +
                       "3. 闪烁结束后，按相同顺序点击格子\n" +
                       "4. 选对所有格子即可过关\n" +
                       "5. 选错格子则游戏结束",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "游戏设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlowPurple
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "网格大小",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GridSizeOption(
                    size = 3,
                    label = "3×3",
                    isSelected = currentGridSize == 3,
                    onClick = { onGridSizeSelected(3) }
                )
                
                GridSizeOption(
                    size = 4,
                    label = "4×4",
                    isSelected = currentGridSize == 4,
                    onClick = { onGridSizeSelected(4) }
                )
                
                GridSizeOption(
                    size = 5,
                    label = "5×5",
                    isSelected = currentGridSize == 5,
                    onClick = { onGridSizeSelected(5) }
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
                        text = "干扰项模式",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "开启后会出现黄色干扰格子",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = showDistractors,
                    onCheckedChange = onShowDistractorsChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
private fun RowScope.GridSizeOption(
    size: Int,
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
            onClick = onClick,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = NeonCyan,
                unselectedColor = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonCyan else TextSecondary
        )
    }
}

@Composable
private fun GameOverContent(
    gameState: GameState,
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
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ErrorRed.copy(alpha = 0.3f)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(ErrorRed.copy(alpha = 0.5f), NeonOrange.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
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
                    color = ErrorRed
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "最终成绩",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Level ${gameState.currentLevel}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "成功记忆了 ${gameState.flashCount - 1} 个格子",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CyberButton(
            onClick = onRetry,
            text = "重试"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "返回游戏选择",
                fontSize = 16.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CyberButton(
    onClick: () -> Unit,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = NeonCyan.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun buildAllCells(
    gridSize: Int,
    flashingSequence: List<GridCell>,
    userSelections: List<GridCell>
): List<GridCell> {
    val allCells = mutableListOf<GridCell>()
    
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val flashingCell = flashingSequence.find { it.row == row && it.col == col }
            val selectedCell = userSelections.find { it.row == row && it.col == col }
            
            val cell = when {
                selectedCell != null -> selectedCell
                flashingCell != null -> flashingCell
                else -> GridCell(row, col)
            }
            
            allCells.add(cell)
        }
    }
    
    return allCells
}
