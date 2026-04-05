package top.alan.memorush.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.NBackGameState
import top.alan.memorush.model.NBackStimulus
import top.alan.memorush.model.StimulusMode

@Composable
fun NBackGameScreen(
    viewModel: NBackGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (gameState.gamePhase) {
            GamePhase.IDLE -> {
                IdleContent(
                    gameState = gameState,
                    onStartGame = { viewModel.startGame() },
                    onNLevelChange = { viewModel.setNLevel(it) },
                    onModeChange = { viewModel.setStimulusMode(it) },
                    onTrialsChange = { viewModel.setTotalTrials(it) }
                )
            }
            GamePhase.SHOWING_SEQUENCE -> {
                GameContent(
                    gameState = gameState,
                    onMatchClick = { viewModel.onMatchResponse() },
                    onNoMatchClick = { viewModel.onNoMatchResponse() }
                )
            }
            GamePhase.GAME_OVER -> {
                GameOverContent(
                    gameState = gameState,
                    onRestart = { viewModel.startGame() },
                    onReset = { viewModel.resetGame() }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun IdleContent(
    gameState: NBackGameState,
    onStartGame: () -> Unit,
    onNLevelChange: (Int) -> Unit,
    onModeChange: (StimulusMode) -> Unit,
    onTrialsChange: (Int) -> Unit
) {
    Text(
        text = "N-Back 任务",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "判断当前项目是否与 ${gameState.nLevel} 步前的项目相同",
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
                text = "N 级别: ${gameState.nLevel}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Slider(
                value = gameState.nLevel.toFloat(),
                onValueChange = { onNLevelChange(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "显示模式",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                StimulusMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = StimulusMode.entries.size
                        ),
                        onClick = { onModeChange(mode) },
                        selected = gameState.stimulusMode == mode
                    ) {
                        Text(
                            when (mode) {
                                StimulusMode.LETTER -> "字母"
                                StimulusMode.POSITION -> "位置"
                                StimulusMode.DUAL -> "双重"
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "试验次数: ${gameState.totalTrials}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Slider(
                value = gameState.totalTrials.toFloat(),
                onValueChange = { onTrialsChange(it.toInt()) },
                valueRange = 10f..50f,
                steps = 7,
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
    gameState: NBackGameState,
    onMatchClick: () -> Unit,
    onNoMatchClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "N = ${gameState.nLevel}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "得分: ${gameState.score}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    LinearProgressIndicator(
        progress = { (gameState.currentTrial + 1).toFloat() / gameState.totalTrials },
        modifier = Modifier.fillMaxWidth()
    )
    
    Text(
        text = "${gameState.currentTrial + 1} / ${gameState.totalTrials}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    when (gameState.stimulusMode) {
        StimulusMode.LETTER -> {
            LetterStimulusDisplay(
                stimulus = gameState.currentStimulus,
                showFeedback = gameState.showFeedback,
                isCorrect = gameState.lastResponseCorrect
            )
        }
        StimulusMode.POSITION -> {
            PositionStimulusDisplay(
                position = gameState.currentPosition,
                showFeedback = gameState.showFeedback,
                isCorrect = gameState.lastResponseCorrect
            )
        }
        StimulusMode.DUAL -> {
            DualStimulusDisplay(
                stimulus = gameState.currentStimulus,
                position = gameState.currentPosition,
                showFeedback = gameState.showFeedback,
                isCorrect = gameState.lastResponseCorrect
            )
        }
    }
    
    Spacer(modifier = Modifier.weight(1f))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onMatchClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = gameState.currentStimulus != null
        ) {
            Text("匹配", fontSize = 18.sp)
        }
        
        Button(
            onClick = onNoMatchClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            enabled = gameState.currentStimulus != null
        ) {
            Text("不匹配", fontSize = 18.sp)
        }
    }
}

@Composable
private fun LetterStimulusDisplay(
    stimulus: NBackStimulus?,
    showFeedback: Boolean,
    isCorrect: Boolean?
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            showFeedback && isCorrect == true -> Color(0xFF4CAF50).copy(alpha = 0.3f)
            showFeedback && isCorrect == false -> Color(0xFFF44336).copy(alpha = 0.3f)
            stimulus != null -> stimulus.color.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "bg_color"
    )
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = stimulus?.color ?: MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (stimulus != null) {
            Text(
                text = stimulus.letter,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = stimulus.color
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PositionStimulusDisplay(
    position: Int?,
    showFeedback: Boolean,
    isCorrect: Boolean?
) {
    val gridColor by animateColorAsState(
        targetValue = when {
            showFeedback && isCorrect == true -> Color(0xFF4CAF50).copy(alpha = 0.3f)
            showFeedback && isCorrect == false -> Color(0xFFF44336).copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "grid_color"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(gridColor),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(9) { index ->
                val isHighlighted = position == index
                val cellColor = if (isHighlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(cellColor)
                )
            }
        }
    }
}

@Composable
private fun DualStimulusDisplay(
    stimulus: NBackStimulus?,
    position: Int?,
    showFeedback: Boolean,
    isCorrect: Boolean?
) {
    val gridColor by animateColorAsState(
        targetValue = when {
            showFeedback && isCorrect == true -> Color(0xFF4CAF50).copy(alpha = 0.3f)
            showFeedback && isCorrect == false -> Color(0xFFF44336).copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "grid_color"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(gridColor),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(9) { index ->
                val isHighlighted = position == index
                val cellColor = if (isHighlighted && stimulus != null) {
                    stimulus.color
                } else {
                    MaterialTheme.colorScheme.surface
                }
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(cellColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHighlighted && stimulus != null) {
                        Text(
                            text = stimulus.letter,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverContent(
    gameState: NBackGameState,
    onRestart: () -> Unit,
    onReset: () -> Unit
) {
    val accuracy = if (gameState.totalResponses > 0) {
        (gameState.correctResponses.toFloat() / gameState.totalResponses * 100).toInt()
    } else {
        0
    }
    
    Text(
        text = "游戏结束",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
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
                text = "最终得分",
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "正确率",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$accuracy%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "N 级别",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.nLevel}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "正确次数",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.correctResponses}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "总响应次数",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.totalResponses}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text("返回设置", fontSize = 16.sp)
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

@Composable
private fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
