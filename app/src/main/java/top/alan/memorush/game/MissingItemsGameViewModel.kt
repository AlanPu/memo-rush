package top.alan.memorush.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.ItemIcon
import top.alan.memorush.model.MissingItemsGameState
import top.alan.memorush.model.SceneItem
import kotlin.math.sqrt
import kotlin.random.Random

class MissingItemsGameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(MissingItemsGameState())
    val gameState: StateFlow<MissingItemsGameState> = _gameState.asStateFlow()
    
    private var gameJob: Job? = null
    
    private val itemColors = listOf(
        Color(0xFFE53935),
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFB8C00),
        Color(0xFF8E24AA),
        Color(0xFF00ACC1),
        Color(0xFF3949AB),
        Color(0xFFD81B60),
        Color(0xFF7CB342),
        Color(0xFFFFB300)
    )
    
    fun startGame() {
        gameJob?.cancel()
        
        val state = _gameState.value
        val items = generateItems(state.totalItems)
        val missingItems = items.shuffled().take(state.missingCount)
        val remainingItems = items.filter { it !in missingItems }
        
        _gameState.value = state.copy(
            items = items,
            missingItems = missingItems,
            foundItems = emptyList(),
            gamePhase = GamePhase.SHOWING_SEQUENCE,
            attemptsRemaining = 3,
            showHint = false
        )
        
        startObservationPhase()
    }
    
    private fun generateItems(count: Int): List<SceneItem> {
        val items = mutableListOf<SceneItem>()
        val usedPositions = mutableSetOf<IntOffset>()
        
        val areaWidth = 350
        val areaHeight = 300
        val itemSize = 48
        val padding = 30
        
        for (i in 0 until count) {
            var position: IntOffset
            var attempts = 0
            
            do {
                val x = Random.nextInt(padding, areaWidth - itemSize - padding)
                val y = Random.nextInt(padding, areaHeight - itemSize - padding)
                position = IntOffset(x, y)
                attempts++
            } while (usedPositions.any { 
                sqrt(
                    (it.x - position.x).toFloat() * (it.x - position.x) +
                    (it.y - position.y).toFloat() * (it.y - position.y)
                ) < (itemSize + 20)
            } && attempts < 100)
            
            usedPositions.add(position)
            
            items.add(
                SceneItem(
                    id = i,
                    name = ItemIcon.entries[i % ItemIcon.entries.size].displayName,
                    icon = ItemIcon.entries[i % ItemIcon.entries.size],
                    color = itemColors[i % itemColors.size],
                    position = position,
                    size = itemSize
                )
            )
        }
        
        return items
    }
    
    private fun startObservationPhase() {
        gameJob = viewModelScope.launch {
            delay(_gameState.value.observationTime)
            
            val state = _gameState.value
            val remainingItems = state.items.filter { it !in state.missingItems }
            
            _gameState.value = state.copy(
                items = remainingItems,
                gamePhase = GamePhase.USER_INPUT
            )
        }
    }
    
    fun onPositionClick(x: Int, y: Int) {
        val state = _gameState.value
        if (state.gamePhase != GamePhase.USER_INPUT) return
        
        val clickThreshold = 60
        
        val unfoundMissingItems = state.missingItems.filter { missingItem ->
            state.foundItems.none { it.id == missingItem.id }
        }
        
        val foundItem = unfoundMissingItems.find { item ->
            val distance = sqrt(
                (item.position.x - x).toFloat() * (item.position.x - x) +
                (item.position.y - y).toFloat() * (item.position.y - y)
            )
            distance <= clickThreshold
        }
        
        if (foundItem != null) {
            val newFoundItems = state.foundItems + foundItem
            val newScore = state.score + 10
            val newItems = state.items + foundItem
            
            _gameState.value = state.copy(
                foundItems = newFoundItems,
                score = newScore,
                items = newItems
            )
            
            if (newFoundItems.size == state.missingItems.size) {
                levelComplete()
            }
        } else {
            val newAttempts = state.attemptsRemaining - 1
            
            if (newAttempts <= 0) {
                gameOver()
            } else {
                _gameState.value = state.copy(
                    attemptsRemaining = newAttempts
                )
            }
        }
    }
    
    fun onItemClick(item: SceneItem) {
        val state = _gameState.value
        if (state.gamePhase != GamePhase.USER_INPUT) return
        
        val newAttempts = state.attemptsRemaining - 1
        
        if (newAttempts <= 0) {
            gameOver()
        } else {
            _gameState.value = state.copy(
                attemptsRemaining = newAttempts
            )
        }
    }
    
    private fun levelComplete() {
        val state = _gameState.value
        _gameState.value = state.copy(
            gamePhase = GamePhase.LEVEL_COMPLETE
        )
    }
    
    fun nextLevel() {
        val state = _gameState.value
        val newLevel = state.currentLevel + 1
        val newTotalItems = minOf(state.totalItems + 1, 12)
        val newMissingCount = minOf(state.missingCount + if (newLevel % 3 == 0) 1 else 0, 4)
        val newObservationTime = maxOf(state.observationTime - 500, 2000)
        
        _gameState.value = state.copy(
            currentLevel = newLevel,
            totalItems = newTotalItems,
            missingCount = newMissingCount,
            observationTime = newObservationTime,
            gamePhase = GamePhase.IDLE
        )
    }
    
    private fun gameOver() {
        val state = _gameState.value
        _gameState.value = state.copy(
            gamePhase = GamePhase.GAME_OVER
        )
    }
    
    fun showHint() {
        val state = _gameState.value
        if (state.gamePhase == GamePhase.USER_INPUT) {
            _gameState.value = state.copy(
                showHint = true,
                score = maxOf(state.score - 5, 0)
            )
        }
    }
    
    fun setTotalItems(count: Int) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(totalItems = count.coerceIn(3, 12))
        }
    }
    
    fun setMissingCount(count: Int) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(missingCount = count.coerceIn(1, 4))
        }
    }
    
    fun setObservationTime(time: Long) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(observationTime = time.coerceIn(2000, 10000))
        }
    }
    
    fun resetGame() {
        gameJob?.cancel()
        _gameState.value = MissingItemsGameState()
    }
}
