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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.NBackGameState
import top.alan.memorush.model.NBackStimulus
import top.alan.memorush.model.StimulusMode
import top.alan.memorush.ui.theme.CardBackground
import top.alan.memorush.ui.theme.DarkBackground
import top.alan.memorush.ui.theme.ErrorRed
import top.alan.memorush.ui.theme.GlowPink
import top.alan.memorush.ui.theme.GlowPurple
import top.alan.memorush.ui.theme.GradientEnd
import top.alan.memorush.ui.theme.GradientMiddle
import top.alan.memorush.ui.theme.GradientStart
import top.alan.memorush.ui.theme.NeonCyan
import top.alan.memorush.ui.theme.NeonPurple
import top.alan.memorush.ui.theme.SuccessGreen
import top.alan.memorush.ui.theme.TextMuted
import top.alan.memorush.ui.theme.TextPrimary
import top.alan.memorush.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NBackGameScreen(
    onBack: () -> Unit,
    viewModel: NBackGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "N-Back 任务",
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
                    IconButton(onClick = {
                        viewModel.resetGame()
                        onBack()
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                ),
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
                        onReset = { viewModel.resetGame() },
                        onBack = onBack
                    )
                }
                else -> {}
            }
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
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "判断当前项目是否与 ${gameState.nLevel} 步前的项目相同",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "N 级别: ${gameState.nLevel}",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            
            Slider(
                value = gameState.nLevel.toFloat(),
                onValueChange = { onNLevelChange(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "显示模式",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
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
                        selected = gameState.stimulusMode == mode,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = NeonCyan.copy(alpha = 0.2f),
                            activeContentColor = NeonCyan,
                            inactiveContainerColor = CardBackground,
                            inactiveContentColor = TextSecondary
                        )
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
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            
            Slider(
                value = gameState.totalTrials.toFloat(),
                onValueChange = { onTrialsChange(it.toInt()) },
                valueRange = 10f..50f,
                steps = 7,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                )
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    CyberButton(
        onClick = onStartGame,
        text = "开始游戏"
    )
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
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Text(
            text = "得分: ${gameState.score}",
            style = MaterialTheme.typography.titleMedium,
            color = NeonCyan
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
        color = TextMuted
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
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan.copy(alpha = 0.2f),
                contentColor = NeonCyan
            ),
            enabled = gameState.currentStimulus != null
        ) {
            Text("匹配", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = onNoMatchClick,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GlowPurple.copy(alpha = 0.2f),
                contentColor = GlowPurple
            ),
            enabled = gameState.currentStimulus != null
        ) {
            Text("不匹配", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            showFeedback && isCorrect == true -> SuccessGreen.copy(alpha = 0.3f)
            showFeedback && isCorrect == false -> ErrorRed.copy(alpha = 0.3f)
            stimulus != null -> stimulus.color.copy(alpha = 0.2f)
            else -> CardBackground
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
                brush = Brush.linearGradient(
                    colors = listOf(stimulus?.color ?: NeonCyan, NeonPurple)
                ),
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
                color = NeonCyan
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
            showFeedback && isCorrect == true -> SuccessGreen.copy(alpha = 0.3f)
            showFeedback && isCorrect == false -> ErrorRed.copy(alpha = 0.3f)
            else -> CardBackground
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
                .background(gridColor)
                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(9) { index ->
                val isHighlighted = position == index
                val cellColor = if (isHighlighted) {
                    NeonCyan
                } else {
                    CardBackground
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
            showFeedback && isCorrect == true -> SuccessGreen.copy(alpha = 0.3f)
            showFeedback && isCorrect == false -> ErrorRed.copy(alpha = 0.3f)
            else -> CardBackground
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
                .background(gridColor)
                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(9) { index ->
                val isHighlighted = position == index
                val cellColor = if (isHighlighted && stimulus != null) {
                    stimulus.color
                } else {
                    CardBackground
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
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val accuracy = if (gameState.totalResponses > 0) {
        (gameState.correctResponses.toFloat() / gameState.totalResponses * 100).toInt()
    } else {
        0
    }
    
    Text(
        text = "游戏结束",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = NeonCyan.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.5f), GlowPurple.copy(alpha = 0.3f))
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "最终得分",
                style = MaterialTheme.typography.titleMedium,
                color = TextMuted
            )
            
            Text(
                text = "${gameState.score}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
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
                        color = TextMuted
                    )
                    Text(
                        text = "$accuracy%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "N 级别",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "${gameState.nLevel}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GlowPurple
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
                        color = TextMuted
                    )
                    Text(
                        text = "${gameState.correctResponses}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "总响应次数",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "${gameState.totalResponses}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
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
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextSecondary
            )
        ) {
            Text("返回主界面", fontSize = 16.sp)
        }
        
        CyberButton(
            onClick = onRestart,
            text = "再玩一次",
            modifier = Modifier.weight(1f)
        )
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
            .background(CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress())
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeonCyan, GlowPurple)
                    )
                )
        )
    }
}

@Composable
private fun CyberButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
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
