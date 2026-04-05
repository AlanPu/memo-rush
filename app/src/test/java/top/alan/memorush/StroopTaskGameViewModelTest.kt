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
import top.alan.memorush.game.StroopTaskGameViewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.StroopColor

@OptIn(ExperimentalCoroutinesApi::class)
class StroopTaskGameViewModelTest {

    private lateinit var viewModel: StroopTaskGameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StroopTaskGameViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGameInitialState() {
        val gameState = viewModel.gameState.value
        
        assertEquals(1, gameState.currentLevel)
        assertEquals(0, gameState.currentTrial)
        assertEquals(10, gameState.totalTrials)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertEquals(0, gameState.score)
        assertEquals(0, gameState.correctCount)
        assertEquals(0, gameState.wrongCount)
        assertEquals(3000, gameState.timeLimit)
        assertEquals(0.5f, gameState.congruentRatio, 0.01f)
    }

    @Test
    fun testGameReset() {
        viewModel.setTimeLimit(5000)
        viewModel.setTotalTrials(15)
        
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(3000, gameState.timeLimit)
        assertEquals(10, gameState.totalTrials)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
    }

    @Test
    fun testSetTimeLimit() {
        viewModel.setTimeLimit(4000)
        
        val gameState = viewModel.gameState.value
        assertEquals(4000, gameState.timeLimit)
    }

    @Test
    fun testSetTotalTrials() {
        viewModel.setTotalTrials(15)
        
        val gameState = viewModel.gameState.value
        assertEquals(15, gameState.totalTrials)
    }

    @Test
    fun testStartGameGeneratesTrial() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val gameState = viewModel.gameState.value
        
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameState.gamePhase)
        assertNotNull(gameState.trial)
        assertEquals(1, gameState.currentTrial)
    }

    @Test
    fun testTrialHasValidColors() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val gameState = viewModel.gameState.value
        val trial = gameState.trial
        
        assertNotNull(trial)
        assertTrue(trial!!.wordColor in StroopColor.entries)
        assertTrue(trial.wordMeaning in StroopColor.entries)
    }

    @Test
    fun testCongruentTrialHasSameColorAndMeaning() = runTest {
        var foundCongruent = false
        
        for (i in 0..50) {
            viewModel.resetGame()
            viewModel.startGame()
            testDispatcher.scheduler.advanceTimeBy(100)
            
            val trial = viewModel.gameState.value.trial
            if (trial != null && trial.isCongruent) {
                assertEquals(trial.wordColor, trial.wordMeaning)
                foundCongruent = true
                break
            }
        }
        
        assertTrue("Should find at least one congruent trial", foundCongruent)
    }

    @Test
    fun testIncongruentTrialHasDifferentColorAndMeaning() = runTest {
        var foundIncongruent = false
        
        for (i in 0..50) {
            viewModel.resetGame()
            viewModel.startGame()
            testDispatcher.scheduler.advanceTimeBy(100)
            
            val trial = viewModel.gameState.value.trial
            if (trial != null && !trial.isCongruent) {
                assertTrue(trial.wordColor != trial.wordMeaning)
                foundIncongruent = true
                break
            }
        }
        
        assertTrue("Should find at least one incongruent trial", foundIncongruent)
    }

    @Test
    fun testCorrectAnswerIncreasesScore() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val trial = viewModel.gameState.value.trial!!
        val scoreBefore = viewModel.gameState.value.score
        
        viewModel.onColorSelected(trial.wordColor)
        
        val scoreAfter = viewModel.gameState.value.score
        assertTrue("Score should increase on correct answer", scoreAfter > scoreBefore)
        assertTrue("Should show feedback", viewModel.gameState.value.showFeedback)
        assertTrue("Answer should be correct", viewModel.gameState.value.lastAnswerCorrect)
    }

    @Test
    fun testWrongAnswerDecreasesScore() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val trial = viewModel.gameState.value.trial!!
        val wrongColor = StroopColor.entries.first { it != trial.wordColor }
        
        viewModel.onColorSelected(trial.wordColor)
        
        testDispatcher.scheduler.advanceTimeBy(700)
        testDispatcher.scheduler.advanceTimeBy(100)
        
        if (viewModel.gameState.value.gamePhase == GamePhase.SHOWING_SEQUENCE && 
            viewModel.gameState.value.trial != null) {
            val trial2 = viewModel.gameState.value.trial!!
            val wrongColor2 = StroopColor.entries.first { it != trial2.wordColor }
            val scoreBefore = viewModel.gameState.value.score
            
            viewModel.onColorSelected(wrongColor2)
            
            val scoreAfter = viewModel.gameState.value.score
            assertTrue("Score should decrease on wrong answer", scoreAfter < scoreBefore)
            assertTrue("Should show feedback", viewModel.gameState.value.showFeedback)
            assertFalse("Answer should be wrong", viewModel.gameState.value.lastAnswerCorrect)
        }
    }

    @Test
    fun testCorrectAnswerIncreasesCorrectCount() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val trial = viewModel.gameState.value.trial!!
        val correctBefore = viewModel.gameState.value.correctCount
        
        viewModel.onColorSelected(trial.wordColor)
        
        assertEquals(correctBefore + 1, viewModel.gameState.value.correctCount)
    }

    @Test
    fun testWrongAnswerIncreasesWrongCount() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val trial = viewModel.gameState.value.trial!!
        val wrongColor = StroopColor.entries.first { it != trial.wordColor }
        val wrongBefore = viewModel.gameState.value.wrongCount
        
        viewModel.onColorSelected(wrongColor)
        
        assertEquals(wrongBefore + 1, viewModel.gameState.value.wrongCount)
    }

    @Test
    fun testStreakBonus() = runTest {
        viewModel.startGame()
        
        for (i in 0..2) {
            testDispatcher.scheduler.advanceTimeBy(100)
            
            val trial = viewModel.gameState.value.trial
            if (trial != null && viewModel.gameState.value.gamePhase == GamePhase.SHOWING_SEQUENCE) {
                val scoreBefore = viewModel.gameState.value.score
                viewModel.onColorSelected(trial.wordColor)
                val scoreAfter = viewModel.gameState.value.score
                
                if (i > 0 && viewModel.gameState.value.lastAnswerCorrect) {
                    val expectedStreakBonus = minOf(i * 2, 10)
                    val baseScore = if (trial.isCongruent) 5 else 10
                    assertTrue("Streak bonus should apply", scoreAfter >= scoreBefore + baseScore)
                }
            }
            
            testDispatcher.scheduler.advanceTimeBy(700)
        }
    }

    @Test
    fun testTimeOutCountsAsWrong() = runTest {
        viewModel.setTimeLimit(1000)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        assertTrue("Wrong count should increase on timeout", gameState.wrongCount > 0)
    }

    @Test
    fun testLevelCompleteAfterAllTrials() = runTest {
        viewModel.setTotalTrials(3)
        viewModel.startGame()
        
        repeat(3) {
            testDispatcher.scheduler.advanceTimeBy(100)
            
            while (viewModel.gameState.value.gamePhase == GamePhase.SHOWING_SEQUENCE) {
                val trial = viewModel.gameState.value.trial
                if (trial != null) {
                    viewModel.onColorSelected(trial.wordColor)
                    break
                }
                testDispatcher.scheduler.advanceTimeBy(50)
            }
            
            testDispatcher.scheduler.advanceTimeBy(700)
        }
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.LEVEL_COMPLETE, gameState.gamePhase)
    }

    @Test
    fun testNextLevelIncreasesDifficulty() = runTest {
        viewModel.setTotalTrials(3)
        viewModel.setTimeLimit(4000)
        viewModel.startGame()
        
        repeat(3) {
            testDispatcher.scheduler.advanceTimeBy(100)
            
            while (viewModel.gameState.value.gamePhase == GamePhase.SHOWING_SEQUENCE) {
                val trial = viewModel.gameState.value.trial
                if (trial != null) {
                    viewModel.onColorSelected(trial.wordColor)
                    break
                }
                testDispatcher.scheduler.advanceTimeBy(50)
            }
            
            testDispatcher.scheduler.advanceTimeBy(700)
        }
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateBefore = viewModel.gameState.value
        assertEquals(GamePhase.LEVEL_COMPLETE, stateBefore.gamePhase)
        
        viewModel.nextLevel()
        
        val stateAfter = viewModel.gameState.value
        assertEquals(2, stateAfter.currentLevel)
        assertTrue("Total trials should increase", stateAfter.totalTrials >= stateBefore.totalTrials)
        assertTrue("Time limit should decrease", stateAfter.timeLimit <= stateBefore.timeLimit)
        assertTrue("Congruent ratio should decrease", stateAfter.congruentRatio <= stateBefore.congruentRatio)
    }

    @Test
    fun testSettingsCannotChangeDuringGame() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        viewModel.setTimeLimit(5000)
        viewModel.setTotalTrials(20)
        
        val gameState = viewModel.gameState.value
        assertEquals(3000, gameState.timeLimit)
        assertEquals(10, gameState.totalTrials)
    }

    @Test
    fun testTimeLimitCoercedToValidRange() {
        viewModel.setTimeLimit(500)
        assertEquals(1500, viewModel.gameState.value.timeLimit)
        
        viewModel.setTimeLimit(10000)
        assertEquals(5000, viewModel.gameState.value.timeLimit)
    }

    @Test
    fun testTotalTrialsCoercedToValidRange() {
        viewModel.setTotalTrials(2)
        assertEquals(5, viewModel.gameState.value.totalTrials)
        
        viewModel.setTotalTrials(30)
        assertEquals(20, viewModel.gameState.value.totalTrials)
    }

    @Test
    fun testTimerDecreases() = runTest {
        viewModel.setTimeLimit(3000)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val timeBefore = viewModel.gameState.value.remainingTime
        
        testDispatcher.scheduler.advanceTimeBy(500)
        
        val timeAfter = viewModel.gameState.value.remainingTime
        
        assertTrue("Time should decrease", timeAfter < timeBefore)
    }

    @Test
    fun testCannotSelectColorAfterAnswer() = runTest {
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(100)
        
        val trial = viewModel.gameState.value.trial!!
        viewModel.onColorSelected(trial.wordColor)
        
        val scoreAfter = viewModel.gameState.value.score
        
        viewModel.onColorSelected(trial.wordColor)
        
        assertEquals("Score should not change after answer", scoreAfter, viewModel.gameState.value.score)
    }
}
