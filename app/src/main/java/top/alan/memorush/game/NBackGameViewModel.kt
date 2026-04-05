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
import top.alan.memorush.model.NBackGameState
import top.alan.memorush.model.NBackStimulus
import top.alan.memorush.model.StimulusMode
import kotlin.random.Random

class NBackGameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(NBackGameState())
    val gameState: StateFlow<NBackGameState> = _gameState.asStateFlow()
    
    private var gameJob: Job? = null
    
    fun startGame() {
        gameJob?.cancel()
        _gameState.value = NBackGameState(
            nLevel = _gameState.value.nLevel,
            stimulusMode = _gameState.value.stimulusMode,
            totalTrials = _gameState.value.totalTrials,
            stimulusDuration = _gameState.value.stimulusDuration,
            isDualMode = _gameState.value.isDualMode
        )
        
        val sequence = generateStimulusSequence()
        val positionSequence = if (_gameState.value.stimulusMode == StimulusMode.POSITION || 
                                   _gameState.value.stimulusMode == StimulusMode.DUAL) {
            generatePositionSequence()
        } else {
            emptyList()
        }
        
        _gameState.value = _gameState.value.copy(
            stimulusSequence = sequence,
            positionSequence = positionSequence,
            gamePhase = GamePhase.SHOWING_SEQUENCE
        )
        
        startShowingStimuli()
    }
    
    private fun generateStimulusSequence(): List<NBackStimulus> {
        val nLevel = _gameState.value.nLevel
        val totalTrials = _gameState.value.totalTrials
        val sequence = mutableListOf<NBackStimulus>()
        
        for (i in 0 until totalTrials) {
            if (i >= nLevel && Random.nextFloat() < 0.3f) {
                sequence.add(sequence[i - nLevel])
            } else {
                var newStimulus: NBackStimulus
                do {
                    newStimulus = NBackStimulus.random()
                } while (i >= nLevel && newStimulus == sequence[i - nLevel])
                sequence.add(newStimulus)
            }
        }
        
        return sequence
    }
    
    private fun generatePositionSequence(): List<Int> {
        val nLevel = _gameState.value.nLevel
        val totalTrials = _gameState.value.totalTrials
        val sequence = mutableListOf<Int>()
        
        for (i in 0 until totalTrials) {
            if (i >= nLevel && Random.nextFloat() < 0.3f) {
                sequence.add(sequence[i - nLevel])
            } else {
                var newPosition: Int
                do {
                    newPosition = Random.nextInt(9)
                } while (i >= nLevel && newPosition == sequence[i - nLevel])
                sequence.add(newPosition)
            }
        }
        
        return sequence
    }
    
    private fun startShowingStimuli() {
        gameJob = viewModelScope.launch {
            val sequence = _gameState.value.stimulusSequence
            val positionSequence = _gameState.value.positionSequence
            val stimulusDuration = _gameState.value.stimulusDuration
            
            for (i in sequence.indices) {
                _gameState.value = _gameState.value.copy(
                    currentStimulus = sequence[i],
                    currentPosition = if (positionSequence.isNotEmpty()) positionSequence[i] else null,
                    currentTrial = i,
                    showFeedback = false,
                    lastResponseCorrect = null
                )
                
                delay(stimulusDuration)
                
                _gameState.value = _gameState.value.copy(
                    currentStimulus = null,
                    currentPosition = null
                )
                
                delay(500)
            }
            
            endGame()
        }
    }
    
    fun onMatchResponse() {
        val state = _gameState.value
        if (state.gamePhase != GamePhase.SHOWING_SEQUENCE || state.currentStimulus == null) return
        
        val currentIndex = state.currentTrial
        val nLevel = state.nLevel
        
        if (currentIndex < nLevel) {
            showFeedback(false, "前 $nLevel 个项目无法匹配")
            return
        }
        
        val isMatch = when (state.stimulusMode) {
            StimulusMode.LETTER -> {
                state.stimulusSequence[currentIndex] == state.stimulusSequence[currentIndex - nLevel]
            }
            StimulusMode.POSITION -> {
                state.positionSequence[currentIndex] == state.positionSequence[currentIndex - nLevel]
            }
            StimulusMode.DUAL -> {
                state.stimulusSequence[currentIndex] == state.stimulusSequence[currentIndex - nLevel] &&
                state.positionSequence[currentIndex] == state.positionSequence[currentIndex - nLevel]
            }
        }
        
        val isCorrect = isMatch
        updateScore(isCorrect)
        showFeedback(isCorrect, if (isCorrect) "正确！" else "错误！这不是匹配")
    }
    
    fun onNoMatchResponse() {
        val state = _gameState.value
        if (state.gamePhase != GamePhase.SHOWING_SEQUENCE || state.currentStimulus == null) return
        
        val currentIndex = state.currentTrial
        val nLevel = state.nLevel
        
        if (currentIndex < nLevel) {
            showFeedback(true, "正确！前 $nLevel 个项目无法匹配")
            updateScore(true)
            return
        }
        
        val isMatch = when (state.stimulusMode) {
            StimulusMode.LETTER -> {
                state.stimulusSequence[currentIndex] == state.stimulusSequence[currentIndex - nLevel]
            }
            StimulusMode.POSITION -> {
                state.positionSequence[currentIndex] == state.positionSequence[currentIndex - nLevel]
            }
            StimulusMode.DUAL -> {
                state.stimulusSequence[currentIndex] == state.stimulusSequence[currentIndex - nLevel] &&
                state.positionSequence[currentIndex] == state.positionSequence[currentIndex - nLevel]
            }
        }
        
        val isCorrect = !isMatch
        updateScore(isCorrect)
        showFeedback(isCorrect, if (isCorrect) "正确！" else "错误！这其实是匹配")
    }
    
    private fun updateScore(isCorrect: Boolean) {
        val state = _gameState.value
        _gameState.value = state.copy(
            totalResponses = state.totalResponses + 1,
            correctResponses = if (isCorrect) state.correctResponses + 1 else state.correctResponses,
            score = if (isCorrect) state.score + 10 else state.score
        )
    }
    
    private fun showFeedback(isCorrect: Boolean, message: String) {
        _gameState.value = _gameState.value.copy(
            showFeedback = true,
            lastResponseCorrect = isCorrect
        )
    }
    
    private fun endGame() {
        val state = _gameState.value
        val accuracy = if (state.totalResponses > 0) {
            state.correctResponses.toFloat() / state.totalResponses
        } else {
            0f
        }
        
        _gameState.value = state.copy(
            gamePhase = GamePhase.GAME_OVER,
            currentStimulus = null,
            currentPosition = null
        )
    }
    
    fun setNLevel(level: Int) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(nLevel = level)
        }
    }
    
    fun setStimulusMode(mode: StimulusMode) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(stimulusMode = mode)
        }
    }
    
    fun setTotalTrials(trials: Int) {
        if (_gameState.value.gamePhase == GamePhase.IDLE) {
            _gameState.value = _gameState.value.copy(totalTrials = trials)
        }
    }
    
    fun resetGame() {
        gameJob?.cancel()
        _gameState.value = NBackGameState()
    }
}
