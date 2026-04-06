package top.alan.memorush.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.ItemShape
import top.alan.memorush.model.MissingItemsGameState
import top.alan.memorush.model.SceneItem
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
import top.alan.memorush.ui.theme.WarningOrange
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingItemsGameScreen(
    onBack: () -> Unit,
    viewModel: MissingItemsGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "消失的物品",
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
                        onTotalItemsChange = { viewModel.setTotalItems(it) },
                        onMissingCountChange = { viewModel.setMissingCount(it) },
                        onObservationTimeChange = { viewModel.setObservationTime(it) }
                    )
                }
                GamePhase.SHOWING_SEQUENCE -> {
                    ObservationContent(
                        gameState = gameState
                    )
                }
                GamePhase.USER_INPUT -> {
                    GameContent(
                        gameState = gameState,
                        onItemClick = { viewModel.onItemClick(it) },
                        onPositionClick = { x, y -> viewModel.onPositionClick(x, y) },
                        onShowHint = { viewModel.showHint() }
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
    gameState: MissingItemsGameState,
    onStartGame: () -> Unit,
    onTotalItemsChange: (Int) -> Unit,
    onMissingCountChange: (Int) -> Unit,
    onObservationTimeChange: (Long) -> Unit
) {
    Text(
        text = "消失的物品",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "记住场景中的物品，找出消失的那些",
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
                text = "物品数量: ${gameState.totalItems}",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            
            Slider(
                value = gameState.totalItems.toFloat(),
                onValueChange = { onTotalItemsChange(it.toInt()) },
                valueRange = 3f..12f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "消失数量: ${gameState.missingCount}",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            
            Slider(
                value = gameState.missingCount.toFloat(),
                onValueChange = { onMissingCountChange(it.toInt()) },
                valueRange = 1f..4f,
                steps = 2,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "观察时间: ${gameState.observationTime / 1000} 秒",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            
            Slider(
                value = gameState.observationTime.toFloat(),
                onValueChange = { onObservationTimeChange(it.toLong()) },
                valueRange = 2000f..10000f,
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
private fun ColumnScope.ObservationContent(
    gameState: MissingItemsGameState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "关卡 ${gameState.currentLevel}",
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
    
    Text(
        text = "记住这些物品！",
        style = MaterialTheme.typography.titleLarge,
        color = NeonCyan,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    SceneDisplay(
        items = gameState.items,
        enabled = false,
        onItemClick = {},
        showHint = false,
        missingItems = emptyList(),
        onPositionClick = { _, _ -> }
    )
    
    Spacer(modifier = Modifier.weight(1f))
    
    CircularProgressIndicator(
        modifier = Modifier.size(48.dp),
        color = NeonCyan
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "观察中...",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary
    )
}

@Composable
private fun ColumnScope.GameContent(
    gameState: MissingItemsGameState,
    onItemClick: (SceneItem) -> Unit,
    onPositionClick: (Int, Int) -> Unit,
    onShowHint: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "关卡 ${gameState.currentLevel}",
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
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "找出 ${gameState.missingCount} 个消失的物品",
            style = MaterialTheme.typography.titleMedium,
            color = GlowPurple
        )
        
        Text(
            text = "剩余机会: ${gameState.attemptsRemaining}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (gameState.attemptsRemaining <= 1) 
                ErrorRed 
            else 
                TextSecondary
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "已找到: ${gameState.foundItems.size} / ${gameState.missingItems.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = NeonCyan
        )
        
        TextButton(onClick = onShowHint) {
            Text("提示 (-5分)", color = WarningOrange)
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Box(
        modifier = Modifier.weight(1f)
    ) {
        SceneDisplay(
            items = gameState.items,
            enabled = true,
            onItemClick = onItemClick,
            showHint = gameState.showHint,
            missingItems = gameState.missingItems,
            onPositionClick = onPositionClick
        )
    }
}

@Composable
private fun SceneDisplay(
    items: List<SceneItem>,
    enabled: Boolean,
    onItemClick: (SceneItem) -> Unit,
    showHint: Boolean,
    missingItems: List<SceneItem>,
    onPositionClick: (Int, Int) -> Unit
) {
    val density = LocalDensity.current
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled, widthPx, heightPx) {
                    if (enabled) {
                        detectTapGestures { offset ->
                            val normalizedX = (offset.x / widthPx * 350f).roundToInt()
                            val normalizedY = (offset.y / heightPx * 300f).roundToInt()
                            onPositionClick(normalizedX, normalizedY)
                        }
                    }
                }
        ) {
            for (item in items) {
                val x = (item.position.x / 350f * widthPx).roundToInt()
                val y = (item.position.y / 300f * heightPx).roundToInt()
                
                ItemComposable(
                    item = item,
                    isHighlighted = false,
                    enabled = false,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.offset { IntOffset(x, y) }
                )
            }
            
            if (showHint) {
                for (missingItem in missingItems) {
                    if (items.none { it.id == missingItem.id }) {
                        val x = (missingItem.position.x / 350f * widthPx).roundToInt()
                        val y = (missingItem.position.y / 300f * heightPx).roundToInt()
                        
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(x, y) }
                                .size(missingItem.size.dp)
                                .clip(CircleShape)
                                .border(3.dp, WarningOrange, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemComposable(
    item: SceneItem,
    isHighlighted: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(item.size.dp)
            .clip(CircleShape)
            .background(item.color)
            .then(
                if (isHighlighted) {
                    Modifier.border(3.dp, WarningOrange, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ItemShapeComposable(
            shape = item.icon.shape,
            color = Color.White,
            size = (item.size * 0.6).dp
        )
    }
}

@Composable
private fun ItemShapeComposable(
    shape: ItemShape,
    color: Color,
    size: androidx.compose.ui.unit.Dp
) {
    when (shape) {
        ItemShape.CIRCLE -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        ItemShape.SQUARE -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        ItemShape.TRIANGLE -> {
            Text(
                text = "▲",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.DIAMOND -> {
            Text(
                text = "◆",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.STAR -> {
            Text(
                text = "★",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.HEART -> {
            Text(
                text = "♥",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.HEXAGON -> {
            Text(
                text = "⬡",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.PENTAGON -> {
            Text(
                text = "⬠",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.CROSS -> {
            Text(
                text = "✚",
                fontSize = size.value.sp,
                color = color
            )
        }
        ItemShape.MOON -> {
            Text(
                text = "☽",
                fontSize = size.value.sp,
                color = color
            )
        }
    }
}

@Composable
private fun LevelCompleteContent(
    gameState: MissingItemsGameState,
    onNextLevel: () -> Unit
) {
    Text(
        text = "关卡完成！",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = NeonCyan
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
                text = "得分",
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
            
            Text(
                text = "你找到了所有 ${gameState.missingCount} 个消失的物品！",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    CyberButton(
        onClick = onNextLevel,
        text = "下一关"
    )
}

@Composable
private fun GameOverContent(
    gameState: MissingItemsGameState,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
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
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ErrorRed.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(ErrorRed.copy(alpha = 0.5f), GlowPink.copy(alpha = 0.3f))
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
                color = ErrorRed
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "到达关卡: ${gameState.currentLevel}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "找到物品: ${gameState.foundItems.size} / ${gameState.missingItems.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
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
