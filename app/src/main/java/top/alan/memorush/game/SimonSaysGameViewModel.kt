package top.alan.memorush.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.SimonColor
import top.alan.memorush.model.SimonGameState
import kotlin.random.Random

class SimonSaysGameViewModel : ViewModel() {
    
    private val _gameState = MutableStateFlow(SimonGameState())
    val gameState: StateFlow<SimonGameState> = _gameState.asStateFlow()
    
    fun startGame() {
        val currentState = _gameState.value
        val availableColors = SimonColor.values().take(currentState.colorCount)
        
        val sequence = List(currentState.sequenceLength) {
            availableColors.random(Random)
        }
        
        _gameState.value = currentState.copy(
            colorSequence = sequence,
            userSequence = emptyList(),
            gamePhase = GamePhase.SHOWING_SEQUENCE,
            currentInputIndex = 0,
            currentHighlightColor = null
        )
        
        playSequence()
    }
    
    private fun playSequence() {
        viewModelScope.launch {
            delay(800)
            
            val sequence = _gameState.value.colorSequence
            
            for (color in sequence) {
                _gameState.value = _gameState.value.copy(currentHighlightColor = color)
                delay(800)
                _gameState.value = _gameState.value.copy(currentHighlightColor = null)
                delay(400)
            }
            
            delay(300)
            _gameState.value = _gameState.value.copy(gamePhase = GamePhase.USER_INPUT)
        }
    }
    
    fun selectColor(color: SimonColor) {
        val currentState = _gameState.value
        if (currentState.gamePhase != GamePhase.USER_INPUT) return
        
        val expectedSequence = if (currentState.reverseMode) {
            currentState.colorSequence.reversed()
        } else {
            currentState.colorSequence
        }
        
        val expectedColor = expectedSequence.getOrNull(currentState.currentInputIndex)
        val isCorrect = expectedColor == color
        
        val newUserSequence = currentState.userSequence + color
        val newIndex = if (isCorrect) currentState.currentInputIndex + 1 
                      else currentState.currentInputIndex
        
        _gameState.value = currentState.copy(
            userSequence = newUserSequence,
            currentInputIndex = newIndex
        )
        
        when {
            !isCorrect -> {
                _gameState.value = _gameState.value.copy(gamePhase = GamePhase.GAME_OVER)
            }
            newIndex >= expectedSequence.size -> {
                _gameState.value = _gameState.value.copy(gamePhase = GamePhase.LEVEL_COMPLETE)
            }
        }
    }
    
    fun nextLevel() {
        val currentState = _gameState.value
        val maxLength = 20
        
        _gameState.value = SimonGameState(
            colorCount = currentState.colorCount,
            currentLevel = currentState.currentLevel + 1,
            sequenceLength = minOf(currentState.sequenceLength + 1, maxLength),
            reverseMode = currentState.reverseMode
        )
        
        startGame()
    }
    
    fun resetGame() {
        val currentState = _gameState.value
        _gameState.value = SimonGameState(
            colorCount = currentState.colorCount,
            reverseMode = currentState.reverseMode
        )
    }
    
    fun updateColorCount(count: Int) {
        val currentState = _gameState.value
        _gameState.value = SimonGameState(
            colorCount = count,
            reverseMode = currentState.reverseMode
        )
    }
    
    fun updateReverseMode(reverse: Boolean) {
        val currentState = _gameState.value
        _gameState.value = currentState.copy(reverseMode = reverse)
    }
}
