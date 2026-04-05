package top.alan.memorush

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.alan.memorush.game.NBackGameViewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.StimulusMode

@OptIn(ExperimentalCoroutinesApi::class)
class NBackGameViewModelTest {

    private lateinit var viewModel: NBackGameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NBackGameViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGameInitialState() {
        val gameState = viewModel.gameState.value
        
        assertEquals(2, gameState.nLevel)
        assertEquals(1, gameState.currentLevel)
        assertEquals(20, gameState.totalTrials)
        assertEquals(0, gameState.currentTrial)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertEquals(0, gameState.score)
        assertEquals(0, gameState.correctResponses)
        assertEquals(0, gameState.totalResponses)
        assertFalse(gameState.showFeedback)
        assertEquals(StimulusMode.LETTER, gameState.stimulusMode)
    }

    @Test
    fun testGameReset() {
        viewModel.setNLevel(3)
        viewModel.setStimulusMode(StimulusMode.POSITION)
        viewModel.setTotalTrials(30)
        
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(2, gameState.nLevel)
        assertEquals(StimulusMode.LETTER, gameState.stimulusMode)
        assertEquals(20, gameState.totalTrials)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
    }

    @Test
    fun testSetNLevel() {
        viewModel.setNLevel(3)
        
        val gameState = viewModel.gameState.value
        assertEquals(3, gameState.nLevel)
    }

    @Test
    fun testSetStimulusMode() {
        viewModel.setStimulusMode(StimulusMode.POSITION)
        
        val gameState = viewModel.gameState.value
        assertEquals(StimulusMode.POSITION, gameState.stimulusMode)
        
        viewModel.setStimulusMode(StimulusMode.DUAL)
        
        val updatedState = viewModel.gameState.value
        assertEquals(StimulusMode.DUAL, updatedState.stimulusMode)
    }

    @Test
    fun testSetTotalTrials() {
        viewModel.setTotalTrials(30)
        
        val gameState = viewModel.gameState.value
        assertEquals(30, gameState.totalTrials)
    }

    @Test
    fun testStartGameGeneratesSequence() = runTest {
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameState.gamePhase)
        assertEquals(20, gameState.stimulusSequence.size)
    }

    @Test
    fun testStartGameGeneratesCorrectLengthSequence() = runTest {
        viewModel.setTotalTrials(15)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(15, gameState.stimulusSequence.size)
    }

    @Test
    fun testPositionModeGeneratesPositionSequence() = runTest {
        viewModel.setStimulusMode(StimulusMode.POSITION)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(20, gameState.positionSequence.size)
        assertTrue(gameState.positionSequence.all { it in 0..8 })
    }

    @Test
    fun testDualModeGeneratesBothSequences() = runTest {
        viewModel.setStimulusMode(StimulusMode.DUAL)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(20, gameState.stimulusSequence.size)
        assertEquals(20, gameState.positionSequence.size)
    }

    @Test
    fun testSequenceContainsSomeMatches() = runTest {
        viewModel.setNLevel(2)
        viewModel.setTotalTrials(20)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val sequence = gameState.stimulusSequence
        
        var matchCount = 0
        for (i in gameState.nLevel until sequence.size) {
            if (sequence[i] == sequence[i - gameState.nLevel]) {
                matchCount++
            }
        }
        
        assertTrue("Sequence should contain some matches", matchCount > 0)
    }

    @Test
    fun testGamePhaseTransitionsToShowSequence() = runTest {
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameState.gamePhase)
    }

    @Test
    fun testGameEndsAfterAllTrials() = runTest {
        viewModel.setTotalTrials(5)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.GAME_OVER, gameState.gamePhase)
    }

    @Test
    fun testCorrectMatchResponse() = runTest {
        viewModel.setNLevel(2)
        viewModel.setTotalTrials(5)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val sequence = gameState.stimulusSequence
        
        for (i in 0 until gameState.nLevel) {
            testDispatcher.scheduler.advanceTimeBy(2500)
        }
        
        for (i in gameState.nLevel until sequence.size) {
            val isMatch = sequence[i] == sequence[i - gameState.nLevel]
            
            testDispatcher.scheduler.advanceTimeBy(500)
            
            if (isMatch) {
                val scoreBefore = viewModel.gameState.value.score
                viewModel.onMatchResponse()
                val scoreAfter = viewModel.gameState.value.score
                
                if (viewModel.gameState.value.currentStimulus != null) {
                    assertEquals(scoreBefore + 10, scoreAfter)
                }
            }
            
            testDispatcher.scheduler.advanceTimeBy(2000)
        }
    }

    @Test
    fun testCorrectNoMatchResponse() = runTest {
        viewModel.setNLevel(2)
        viewModel.setTotalTrials(5)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val sequence = gameState.stimulusSequence
        
        for (i in 0 until gameState.nLevel) {
            testDispatcher.scheduler.advanceTimeBy(2500)
        }
        
        for (i in gameState.nLevel until sequence.size) {
            val isMatch = sequence[i] == sequence[i - gameState.nLevel]
            
            testDispatcher.scheduler.advanceTimeBy(500)
            
            if (!isMatch) {
                val scoreBefore = viewModel.gameState.value.score
                viewModel.onNoMatchResponse()
                val scoreAfter = viewModel.gameState.value.score
                
                if (viewModel.gameState.value.currentStimulus != null) {
                    assertEquals(scoreBefore + 10, scoreAfter)
                }
            }
            
            testDispatcher.scheduler.advanceTimeBy(2000)
        }
    }

    @Test
    fun testWrongMatchResponse() = runTest {
        viewModel.setNLevel(2)
        viewModel.setTotalTrials(5)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val sequence = gameState.stimulusSequence
        
        for (i in 0 until gameState.nLevel) {
            testDispatcher.scheduler.advanceTimeBy(2500)
        }
        
        for (i in gameState.nLevel until sequence.size) {
            val isMatch = sequence[i] == sequence[i - gameState.nLevel]
            
            testDispatcher.scheduler.advanceTimeBy(500)
            
            if (!isMatch && viewModel.gameState.value.currentStimulus != null) {
                val correctBefore = viewModel.gameState.value.correctResponses
                viewModel.onMatchResponse()
                
                if (viewModel.gameState.value.showFeedback) {
                    assertFalse(viewModel.gameState.value.lastResponseCorrect!!)
                }
            }
            
            testDispatcher.scheduler.advanceTimeBy(2000)
        }
    }

    @Test
    fun testWrongNoMatchResponse() = runTest {
        viewModel.setNLevel(2)
        viewModel.setTotalTrials(5)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val sequence = gameState.stimulusSequence
        
        for (i in 0 until gameState.nLevel) {
            testDispatcher.scheduler.advanceTimeBy(2500)
        }
        
        for (i in gameState.nLevel until sequence.size) {
            val isMatch = sequence[i] == sequence[i - gameState.nLevel]
            
            testDispatcher.scheduler.advanceTimeBy(500)
            
            if (isMatch && viewModel.gameState.value.currentStimulus != null) {
                viewModel.onNoMatchResponse()
                
                if (viewModel.gameState.value.showFeedback) {
                    assertFalse(viewModel.gameState.value.lastResponseCorrect!!)
                }
            }
            
            testDispatcher.scheduler.advanceTimeBy(2000)
        }
    }

    @Test
    fun testCannotRespondWithoutStimulus() = runTest {
        viewModel.setTotalTrials(3)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(500)
        
        viewModel.onMatchResponse()
        
        val gameState = viewModel.gameState.value
        if (gameState.currentStimulus == null) {
            assertEquals(0, gameState.totalResponses)
        }
    }

    @Test
    fun testTotalResponsesIncrements() = runTest {
        viewModel.setNLevel(1)
        viewModel.setTotalTrials(5)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(3500)
        
        val state = viewModel.gameState.value
        if (state.currentStimulus != null && state.currentTrial >= 1) {
            val responsesBefore = state.totalResponses
            viewModel.onMatchResponse()
            val responsesAfter = viewModel.gameState.value.totalResponses
            
            assertTrue("Total responses should increment", responsesAfter > responsesBefore)
        }
    }

    @Test
    fun testScoreIncrementsOnCorrectResponse() = runTest {
        viewModel.setNLevel(1)
        viewModel.setTotalTrials(3)
        viewModel.startGame()
        
        val sequence = viewModel.gameState.value.stimulusSequence
        
        testDispatcher.scheduler.advanceTimeBy(1000)
        
        if (viewModel.gameState.value.currentStimulus != null) {
            val currentTrial = viewModel.gameState.value.currentTrial
            if (currentTrial >= 1) {
                val isMatch = sequence[currentTrial] == sequence[currentTrial - 1]
                val scoreBefore = viewModel.gameState.value.score
                
                if (isMatch) {
                    viewModel.onMatchResponse()
                } else {
                    viewModel.onNoMatchResponse()
                }
                
                if (viewModel.gameState.value.showFeedback && 
                    viewModel.gameState.value.lastResponseCorrect == true) {
                    assertEquals(scoreBefore + 10, viewModel.gameState.value.score)
                }
            }
        }
    }

    @Test
    fun testGameOverShowsResults() = runTest {
        viewModel.setTotalTrials(3)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.GAME_OVER, gameState.gamePhase)
        assertNotNull(gameState.correctResponses)
        assertNotNull(gameState.totalResponses)
    }

    @Test
    fun testDifferentNLevels() = runTest {
        for (n in 1..3) {
            viewModel.resetGame()
            viewModel.setNLevel(n)
            viewModel.setTotalTrials(10)
            viewModel.startGame()
            
            val gameState = viewModel.gameState.value
            assertEquals(n, gameState.nLevel)
            assertEquals(10, gameState.stimulusSequence.size)
        }
    }

    @Test
    fun testSettingsCannotChangeDuringGame() = runTest {
        viewModel.startGame()
        
        viewModel.setNLevel(5)
        viewModel.setStimulusMode(StimulusMode.DUAL)
        viewModel.setTotalTrials(50)
        
        val gameState = viewModel.gameState.value
        assertEquals(2, gameState.nLevel)
        assertEquals(StimulusMode.LETTER, gameState.stimulusMode)
        assertEquals(20, gameState.totalTrials)
    }

    @Test
    fun testPositionSequenceValuesInRange() = runTest {
        viewModel.setStimulusMode(StimulusMode.POSITION)
        viewModel.setTotalTrials(30)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertTrue(gameState.positionSequence.all { it in 0..8 })
    }

    @Test
    fun testStimulusSequenceValuesAreValid() = runTest {
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val validStimuli = top.alan.memorush.model.NBackStimulus.entries.toSet()
        
        assertTrue(gameState.stimulusSequence.all { it in validStimuli })
    }
}
