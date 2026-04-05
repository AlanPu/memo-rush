package top.alan.memorush.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.GameState
import top.alan.memorush.model.GridCell
import kotlin.random.Random

class GridMemoryGameViewModel : ViewModel() {
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    fun startGame() {
        val currentState = _gameState.value
        val totalCells = currentState.gridSize * currentState.gridSize
        val flashCount = minOf(currentState.flashCount, totalCells)
        
        val allCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until currentState.gridSize) {
            for (col in 0 until currentState.gridSize) {
                allCells.add(Pair(row, col))
            }
        }
        
        allCells.shuffle(Random)
        val selectedCells = allCells.take(flashCount).map { (row, col) ->
            GridCell(row, col, isDistractor = false)
        }
        
        val distractorCells = if (currentState.showDistractors && flashCount > 1) {
            val distractorCount = flashCount / 2
            val remainingCells = allCells.drop(flashCount)
            remainingCells.take(distractorCount).map { (row, col) ->
                GridCell(row, col, isDistractor = true)
            }
        } else {
            emptyList()
        }
        
        _gameState.value = currentState.copy(
            flashingSequence = selectedCells + distractorCells,
            userSelections = emptyList(),
            gamePhase = GamePhase.SHOWING_SEQUENCE
        )
        
        playFlashSequence()
    }
    
    private fun playFlashSequence() {
        viewModelScope.launch {
            val sequence = _gameState.value.flashingSequence
            val realCells = sequence.filter { !it.isDistractor }
            val distractorCells = sequence.filter { it.isDistractor }
            
            for (cell in realCells) {
                val currentState = _gameState.value
                val updatedSequence = currentState.flashingSequence.map { 
                    if (it.row == cell.row && it.col == cell.col) {
                        it.copy(isFlashing = true)
                    } else {
                        it.copy(isFlashing = false)
                    }
                }
                _gameState.value = currentState.copy(flashingSequence = updatedSequence)
                
                delay(600)
                
                val stateAfterFlash = _gameState.value
                val resetSequence = stateAfterFlash.flashingSequence.map { 
                    it.copy(isFlashing = false)
                }
                _gameState.value = stateAfterFlash.copy(flashingSequence = resetSequence)
                
                delay(250)
            }
            
            delay(400)
            
            for (cell in distractorCells) {
                val currentState = _gameState.value
                val updatedSequence = currentState.flashingSequence.map { 
                    if (it.row == cell.row && it.col == cell.col) {
                        it.copy(isFlashing = true)
                    } else {
                        it.copy(isFlashing = false)
                    }
                }
                _gameState.value = currentState.copy(flashingSequence = updatedSequence)
                
                delay(450)
                
                val stateAfterFlash = _gameState.value
                val resetSequence = stateAfterFlash.flashingSequence.map { 
                    it.copy(isFlashing = false)
                }
                _gameState.value = stateAfterFlash.copy(flashingSequence = resetSequence)
                
                delay(180)
            }
            
            _gameState.value = _gameState.value.copy(gamePhase = GamePhase.USER_INPUT)
        }
    }
    
    fun selectCell(row: Int, col: Int) {
        val currentState = _gameState.value
        if (currentState.gamePhase != GamePhase.USER_INPUT) return
        
        val alreadySelected = currentState.userSelections.any { 
            it.row == row && it.col == col 
        }
        if (alreadySelected) return
        
        val realFlashCells = currentState.flashingSequence.filter { !it.isDistractor }
        val currentExpectedCell = realFlashCells.getOrNull(currentState.currentSelectionIndex)
        
        val isCorrectOrder = currentExpectedCell != null && 
                            currentExpectedCell.row == row && 
                            currentExpectedCell.col == col
        
        val cellInSequence = currentState.flashingSequence.find { 
            it.row == row && it.col == col 
        }
        
        val isDistractor = cellInSequence?.isDistractor ?: false
        
        val newCell = GridCell(
            row = row,
            col = col,
            isSelected = true,
            isCorrect = isCorrectOrder,
            isDistractor = isDistractor
        )
        
        val newSelections = currentState.userSelections + newCell
        val newIndex = if (isCorrectOrder) currentState.currentSelectionIndex + 1 
                      else currentState.currentSelectionIndex
        
        _gameState.value = currentState.copy(
            userSelections = newSelections,
            currentSelectionIndex = newIndex
        )
        
        val hasError = newSelections.any { it.isCorrect == false }
        val allCorrectCellsSelected = newIndex >= realFlashCells.size
        
        when {
            hasError -> {
                _gameState.value = _gameState.value.copy(gamePhase = GamePhase.GAME_OVER)
            }
            allCorrectCellsSelected -> {
                _gameState.value = _gameState.value.copy(gamePhase = GamePhase.LEVEL_COMPLETE)
            }
        }
    }
    
    fun nextLevel() {
        val currentState = _gameState.value
        val totalCells = currentState.gridSize * currentState.gridSize
        val maxFlashCount = totalCells / 2
        val newFlashCount = minOf(currentState.flashCount + 1, maxFlashCount)
        
        _gameState.value = GameState(
            gridSize = currentState.gridSize,
            currentLevel = currentState.currentLevel + 1,
            flashCount = newFlashCount,
            showDistractors = currentState.showDistractors
        )
        
        startGame()
    }
    
    fun resetGame() {
        val currentState = _gameState.value
        _gameState.value = GameState(
            gridSize = currentState.gridSize,
            flashCount = 2,
            currentLevel = 1,
            showDistractors = currentState.showDistractors
        )
    }
    
    fun updateGridSize(size: Int) {
        val currentState = _gameState.value
        val totalCells = size * size
        val maxFlashCount = totalCells / 2
        val newFlashCount = minOf(currentState.flashCount, maxFlashCount)
        
        _gameState.value = GameState(
            gridSize = size,
            flashCount = newFlashCount,
            currentLevel = 1,
            showDistractors = currentState.showDistractors
        )
    }
    
    fun updateShowDistractors(show: Boolean) {
        val currentState = _gameState.value
        _gameState.value = currentState.copy(showDistractors = show)
    }
}
