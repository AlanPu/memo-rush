package top.alan.memorush.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.StroopColor
import top.alan.memorush.model.StroopTaskGameState
import top.alan.memorush.model.StroopTrial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StroopTaskGameScreen(
    onBack: () -> Unit,
    viewModel: StroopTaskGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "斯特鲁普任务",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameState.gamePhase) {
                GamePhase.IDLE -> {
                    IdleContent(
                        gameState = gameState,
                        onStartGame = { viewModel.startGame() },
                        onTimeLimitChange = { viewModel.setTimeLimit(it) },
                        onTotalTrialsChange = { viewModel.setTotalTrials(it) }
                    )
                }
                GamePhase.SHOWING_SEQUENCE -> {
                    GameContent(
                        gameState = gameState,
                        onColorSelected = { viewModel.onColorSelected(it) }
                    )
                }
                GamePhase.USER_INPUT -> {
                    FeedbackContent(
                        gameState = gameState
                    )
                }
                GamePhase.LEVEL_COMPLETE -> {
                    LevelCompleteContent(
                        gameState = gameState,
                        onNextLevel = { viewModel.nextLevel() }
                    )
                }
                GamePhase.GAME_OVER -> {
                    GameOverContent(
                        gameState = gameState,
                        onRestart = { viewModel.startGame() },
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(
    gameState: StroopTaskGameState,
    onStartGame: () -> Unit,
    onTimeLimitChange: (Long) -> Unit,
    onTotalTrialsChange: (Int) -> Unit
) {
    Text(
        text = "斯特鲁普任务",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "报告词语的字体颜色，忽略词语含义",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "游戏规则",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "• 屏幕会显示一个颜色词语\n• 词语的字体颜色可能与含义不同\n• 选择词语的【字体颜色】而非含义\n• 越快回答得分越高",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "时间限制: ${(gameState.timeLimit / 1000.0).toString().substring(0, 3)} 秒",
                style = MaterialTheme.typography.titleMedium
            )
            
            Slider(
                value = gameState.timeLimit.toFloat(),
                onValueChange = { onTimeLimitChange(it.toLong()) },
                valueRange = 1500f..5000f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "每关题数: ${gameState.totalTrials}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Slider(
                value = gameState.totalTrials.toFloat(),
                onValueChange = { onTotalTrialsChange(it.toInt()) },
                valueRange = 5f..20f,
                steps = 14,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onStartGame,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text("开始游戏", fontSize = 18.sp)
    }
}

@Composable
private fun ColumnScope.GameContent(
    gameState: StroopTaskGameState,
    onColorSelected: (StroopColor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "关卡 ${gameState.currentLevel}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "得分: ${gameState.score}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "题目 ${gameState.currentTrial} / ${gameState.totalTrials}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "正确: ${gameState.correctCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "错误: ${gameState.wrongCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    LinearProgressIndicator(
        progress = { gameState.remainingTime.toFloat() / gameState.timeLimit },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = if (gameState.remainingTime > gameState.timeLimit / 2) {
            MaterialTheme.colorScheme.primary
        } else if (gameState.remainingTime > gameState.timeLimit / 4) {
            Color(0xFFFFA000)
        } else {
            MaterialTheme.colorScheme.error
        },
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    
    Spacer(modifier = Modifier.weight(1f))
    
    gameState.trial?.let { trial ->
        Text(
            text = "选择字体颜色",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        StroopWordDisplay(
            trial = trial,
            showFeedback = gameState.showFeedback,
            isCorrect = gameState.lastAnswerCorrect
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        ColorSelectionGrid(
            onColorSelected = onColorSelected,
            enabled = !gameState.showFeedback
        )
    }
}

@Composable
private fun StroopWordDisplay(
    trial: StroopTrial,
    showFeedback: Boolean,
    isCorrect: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = trial.wordMeaning.displayName,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = trial.wordColor.color
        )
        
        AnimatedVisibility(
            visible = showFeedback,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isCorrect) Color.Green.copy(alpha = 0.3f)
                        else Color.Red.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCorrect) "✓" else "✗",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color.Green else Color.Red
                )
            }
        }
    }
}

@Composable
private fun ColorSelectionGrid(
    onColorSelected: (StroopColor) -> Unit,
    enabled: Boolean
) {
    val colors = StroopColor.entries
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    if (index < colors.size) {
                        ColorButton(
                            stroopColor = colors[index],
                            onClick = { onColorSelected(colors[index]) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorButton(
    stroopColor: StroopColor,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(stroopColor.color)
            .then(
                if (!enabled) {
                    Modifier.background(Color.Black.copy(alpha = 0.3f))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stroopColor.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ColumnScope.FeedbackContent(
    gameState: StroopTaskGameState
) {
    gameState.trial?.let { trial ->
        Text(
            text = "题目 ${gameState.currentTrial} / ${gameState.totalTrials}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        StroopWordDisplay(
            trial = trial,
            showFeedback = true,
            isCorrect = gameState.lastAnswerCorrect
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (!gameState.lastAnswerCorrect) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "正确答案",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(trial.wordColor.color)
                        )
                        Text(
                            text = trial.wordColor.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCompleteContent(
    gameState: StroopTaskGameState,
    onNextLevel: () -> Unit
) {
    Text(
        text = "关卡完成！",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "得分",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "${gameState.score}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "正确",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.correctCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "错误",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.wrongCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "正确率",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(gameState.correctCount.toFloat() / gameState.totalTrials * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onNextLevel,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text("下一关", fontSize = 18.sp)
    }
}

@Composable
private fun GameOverContent(
    gameState: StroopTaskGameState,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Text(
        text = "游戏结束",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "最终得分",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "${gameState.score}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "到达关卡: ${gameState.currentLevel}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "正确率: ${(gameState.correctCount.toFloat() / gameState.totalTrials * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text("返回主界面", fontSize = 16.sp)
        }
        
        Button(
            onClick = onRestart,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text("再玩一次", fontSize = 16.sp)
        }
    }
}
