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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.alan.memorush.game.SimonSaysGameViewModel
import top.alan.memorush.model.GamePhase
import top.alan.memorush.model.SimonColor

@OptIn(ExperimentalCoroutinesApi::class)
class SimonSaysGameViewModelTest {

    private lateinit var viewModel: SimonSaysGameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SimonSaysGameViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGameInitialState() {
        val gameState = viewModel.gameState.value
        
        assertEquals(4, gameState.colorCount)
        assertEquals(1, gameState.currentLevel)
        assertEquals(2, gameState.sequenceLength)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertTrue(gameState.colorSequence.isEmpty())
        assertTrue(gameState.userSequence.isEmpty())
        assertFalse(gameState.reverseMode)
    }

    @Test
    fun testGameReset() {
        viewModel.updateColorCount(6)
        viewModel.updateReverseMode(true)
        
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(6, gameState.colorCount)
        assertEquals(1, gameState.currentLevel)
        assertEquals(2, gameState.sequenceLength)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertTrue(gameState.colorSequence.isEmpty())
        assertTrue(gameState.userSequence.isEmpty())
        assertTrue(gameState.reverseMode)
    }

    @Test
    fun testUpdateColorCount() {
        viewModel.updateColorCount(6)
        
        val gameState = viewModel.gameState.value
        assertEquals(6, gameState.colorCount)
        assertEquals(1, gameState.currentLevel)
    }

    @Test
    fun testUpdateReverseMode() {
        viewModel.updateReverseMode(true)
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.reverseMode)
        
        viewModel.updateReverseMode(false)
        
        val updatedState = viewModel.gameState.value
        assertFalse(updatedState.reverseMode)
    }

    @Test
    fun testSequenceCorrectLength() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(gameState.sequenceLength, gameState.colorSequence.size)
    }

    @Test
    fun testSequenceWithinAvailableColors() = runTest {
        viewModel.updateColorCount(4)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val availableColors = SimonColor.values().take(4).toSet()
        
        assertTrue(gameState.colorSequence.all { it in availableColors })
    }

    @Test
    fun testCorrectColorSelection() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val firstColor = gameState.colorSequence[0]
        
        viewModel.selectColor(firstColor)
        
        val updatedState = viewModel.gameState.value
        assertEquals(1, updatedState.currentInputIndex)
        assertTrue(updatedState.userSequence.contains(firstColor))
    }

    @Test
    fun testWrongColorSelection() = runTest {
        viewModel.updateColorCount(4)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val firstColor = gameState.colorSequence[0]
        val wrongColor = SimonColor.values().take(4).first { it != firstColor }
        
        viewModel.selectColor(wrongColor)
        
        val updatedState = viewModel.gameState.value
        assertEquals(GamePhase.GAME_OVER, updatedState.gamePhase)
    }

    @Test
    fun testLevelCompleteWhenAllCorrectColorsSelected() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        
        for (color in gameState.colorSequence) {
            viewModel.selectColor(color)
        }
        
        val finalState = viewModel.gameState.value
        assertEquals(GamePhase.LEVEL_COMPLETE, finalState.gamePhase)
        assertEquals(gameState.colorSequence.size, finalState.currentInputIndex)
    }

    @Test
    fun testNextLevelIncreasesLevel() = runTest {
        val initialLevel = viewModel.gameState.value.currentLevel
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newLevel = viewModel.gameState.value.currentLevel
        assertEquals(initialLevel + 1, newLevel)
    }

    @Test
    fun testNextLevelIncreasesSequenceLength() = runTest {
        val initialLength = viewModel.gameState.value.sequenceLength
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newLength = viewModel.gameState.value.sequenceLength
        assertTrue(newLength >= initialLength)
    }

    @Test
    fun testNextLevelResetsUserSequence() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        if (gameState.colorSequence.isNotEmpty()) {
            viewModel.selectColor(gameState.colorSequence[0])
        }
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newGameState = viewModel.gameState.value
        assertTrue(newGameState.userSequence.isEmpty())
    }

    @Test
    fun testSequenceLengthMaxLimit() = runTest {
        for (i in 1..25) {
            viewModel.nextLevel()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.sequenceLength <= 20)
    }

    @Test
    fun testGamePhaseTransitionsToShowSequence() = runTest {
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameState.gamePhase)
    }

    @Test
    fun testGamePhaseTransitionsToUserInput() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.USER_INPUT, gameState.gamePhase)
    }

    @Test
    fun testReverseModeCorrectOrder() = runTest {
        viewModel.updateReverseMode(true)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val reversedSequence = gameState.colorSequence.reversed()
        
        for (color in reversedSequence) {
            viewModel.selectColor(color)
        }
        
        val finalState = viewModel.gameState.value
        assertEquals(GamePhase.LEVEL_COMPLETE, finalState.gamePhase)
    }

    @Test
    fun testReverseModeWrongOrder() = runTest {
        viewModel.updateReverseMode(true)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        
        if (gameState.colorSequence.size >= 2) {
            viewModel.selectColor(gameState.colorSequence[0])
            
            val updatedState = viewModel.gameState.value
            assertEquals(GamePhase.GAME_OVER, updatedState.gamePhase)
        }
    }

    @Test
    fun testCurrentInputIndexIncrements() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(0, gameState.currentInputIndex)
        
        if (gameState.colorSequence.isNotEmpty()) {
            viewModel.selectColor(gameState.colorSequence[0])
            assertEquals(1, viewModel.gameState.value.currentInputIndex)
            
            if (gameState.colorSequence.size >= 2) {
                viewModel.selectColor(gameState.colorSequence[1])
                assertEquals(2, viewModel.gameState.value.currentInputIndex)
            }
        }
    }

    @Test
    fun testCurrentInputIndexResetsOnNextLevel() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        
        for (color in gameState.colorSequence) {
            viewModel.selectColor(color)
        }
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newGameState = viewModel.gameState.value
        assertEquals(0, newGameState.currentInputIndex)
    }

    @Test
    fun testColorCountPersistsAfterReset() {
        viewModel.updateColorCount(9)
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(9, gameState.colorCount)
    }

    @Test
    fun testReverseModePersistsAfterReset() {
        viewModel.updateReverseMode(true)
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.reverseMode)
    }

    @Test
    fun testAllColorsAreValidForDifferentCounts() = runTest {
        for (count in listOf(4, 6, 9)) {
            viewModel.updateColorCount(count)
            viewModel.startGame()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val gameState = viewModel.gameState.value
            val availableColors = SimonColor.values().take(count).toSet()
            
            assertTrue("All colors should be valid for count $count", 
                gameState.colorSequence.all { it in availableColors })
        }
    }

    @Test
    fun testColorSelectionProgress() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        
        if (gameState.colorSequence.size >= 2) {
            val firstColor = gameState.colorSequence[0]
            val secondColor = gameState.colorSequence[1]
            
            viewModel.selectColor(firstColor)
            assertEquals(1, viewModel.gameState.value.currentInputIndex)
            
            viewModel.selectColor(secondColor)
            assertEquals(2, viewModel.gameState.value.currentInputIndex)
            
            val finalState = viewModel.gameState.value
            assertEquals(2, finalState.userSequence.size)
        }
    }

    @Test
    fun testCannotSelectColorDuringShowingSequence() = runTest {
        viewModel.startGame()
        
        val gameStateBeforeAdvance = viewModel.gameState.value
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameStateBeforeAdvance.gamePhase)
        
        if (gameStateBeforeAdvance.colorSequence.isNotEmpty()) {
            val firstColor = gameStateBeforeAdvance.colorSequence[0]
            viewModel.selectColor(firstColor)
            
            val updatedState = viewModel.gameState.value
            assertTrue(updatedState.userSequence.isEmpty())
        }
    }
}
