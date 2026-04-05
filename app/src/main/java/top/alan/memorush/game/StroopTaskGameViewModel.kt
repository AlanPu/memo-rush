package top.alan.memorush.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.StroopColor
import top.alan.memorush.model.StroopTaskGameState
import top.alan.memorush.model.StroopTrial
import kotlin.random.Random

class StroopTaskGameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(StroopTaskGameState())
    val gameState: StateFlow<StroopTaskGameState> = _gameState.asStateFlow()
    
    private var timerJob: Job? = null
    
    private val availableColors = StroopColor.entries
    
    fun startGame() {
        timerJob?.cancel()
        
        _gameState.value = StroopTaskGameState(
            currentLevel = 1,
            totalTrials = 10,
            timeLimit = 3000,
            congruentRatio = 0.5f
        )
        
        startNextTrial()
    }
    
    private fun startNextTrial() {
        val state = _gameState.value
        
        if (state.currentTrial >= state.totalTrials) {
            levelComplete()
            return
        }
        
        val trial = generateTrial(state.congruentRatio)
        
        _gameState.value = state.copy(
            trial = trial,
            currentTrial = state.currentTrial + 1,
            gamePhase = GamePhase.SHOWING_SEQUENCE,
            remainingTime = state.timeLimit,
            showFeedback = false
        )
        
        startTimer()
    }
    
    private fun generateTrial(congruentRatio: Float): StroopTrial {
        val wordColor = availableColors.random()
        
        val isCongruent = Random.nextFloat() < congruentRatio
        
        val wordMeaning = if (isCongruent) {
            wordColor
        } else {
            availableColors.filter { it != wordColor }.random()
        }
        
        return StroopTrial(
            wordColor = wordColor,
            wordMeaning = wordMeaning,
            isCongruent = isCongruent
        )
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            val state = _gameState.value
            var remainingTime = state.timeLimit
            
            while (remainingTime > 0 && _gameState.value.gamePhase == GamePhase.SHOWING_SEQUENCE) {
                delay(100)
                remainingTime -= 100
                _gameState.value = _gameState.value.copy(remainingTime = remainingTime)
            }
            
            if (_gameState.value.gamePhase == GamePhase.SHOWING_SEQUENCE) {
                onTimeOut()
            }
        }
    }
    
    private fun onTimeOut() {
        val state = _gameState.value
        
        _gameState.value = state.copy(
            gamePhase = GamePhase.USER_INPUT,
            wrongCount = state.wrongCount + 1,
            showFeedback = true,
            lastAnswerCorrect = false,
            streakCount = 0
        )
        
        viewModelScope.launch {
            delay(800)
            startNextTrial()
        }
    }
    
    fun onColorSelected(selectedColor: StroopColor) {
        val state = _gameState.value
        if (state.gamePhase != GamePhase.SHOWING_SEQUENCE || state.trial == null) return
        
        timerJob?.cancel()
        
        val isCorrect = selectedColor == state.trial.wordColor
        
        val baseScore = if (state.trial.isCongruent) 5 else 10
        val timeBonus = (state.remainingTime.toFloat() / state.timeLimit * 5).toInt()
        val streakBonus = if (isCorrect) minOf(state.streakCount * 2, 10) else 0
        
        val scoreDelta = if (isCorrect) {
            baseScore + timeBonus + streakBonus
        } else {
            -5
        }
        
        _gameState.value = state.copy(
            gamePhase = GamePhase.USER_INPUT,
            score = maxOf(state.score + scoreDelta, 0),
            correctCount = if (isCorrect) state.correctCount + 1 else state.correctCount,
            wrongCount = if (!isCorrect) state.wrongCount + 1 else state.wrongCount,
            showFeedback = true,
            lastAnswerCorrect = isCorrect,
            streakCount = if (isCorrect) state.streakCount + 1 else 0
        )
        
        viewModelScope.launch {
            delay(600)
            startNextTrial()
        }
    }
    
    private fun levelComplete() {
        timerJob?.cancel()
        
        val state = _gameState.value
        _gameState.value = state.copy(
            gamePhase = GamePhase.LEVEL_COMPLETE
        )
    }
    
    fun nextLevel() {
        val state = _gameState.value
        val newLevel = state.currentLevel + 1
        val newTotalTrials = minOf(state.totalTrials + 2, 20)
        val newTimeLimit = maxOf(state.timeLimit - 200, 1500)
        val newCongruentRatio = maxOf(state.congruentRatio - 0.05f, 0.2f)
        
        _gameState.value = state.copy(
            currentLevel = newLevel,
            currentTrial = 0,
            totalTrials = newTotalTrials,
            timeLimit = newTimeLimit,
            congruentRatio = newCongruentRatio,
            correctCount = 0,
            wrongCount = 0,
            streakCount = 0,
            gamePhase = GamePhase.IDLE,
            trial = null
        )
        
        startNextTrial()
    }
    
    fun setTimeLimit(time: Long) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(
                timeLimit = time.coerceIn(1500, 5000),
                remainingTime = time.coerceIn(1500, 5000)
            )
        }
    }
    
    fun setTotalTrials(count: Int) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(
                totalTrials = count.coerceIn(5, 20)
            )
        }
    }
    
    fun resetGame() {
        timerJob?.cancel()
        _gameState.value = StroopTaskGameState()
    }
}
