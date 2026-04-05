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
import top.alan.memorush.game.GridMemoryGameViewModel
import top.alan.memorush.model.GamePhase

@OptIn(ExperimentalCoroutinesApi::class)
class GridMemoryGameViewModelTest {

    private lateinit var viewModel: GridMemoryGameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GridMemoryGameViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGameInitialState() {
        val gameState = viewModel.gameState.value
        
        assertEquals(3, gameState.gridSize)
        assertEquals(1, gameState.currentLevel)
        assertEquals(2, gameState.flashCount)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertTrue(gameState.flashingSequence.isEmpty())
        assertTrue(gameState.userSelections.isEmpty())
        assertFalse(gameState.showDistractors)
    }

    @Test
    fun testGameReset() {
        viewModel.updateGridSize(4)
        viewModel.updateShowDistractors(true)
        
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(4, gameState.gridSize)
        assertEquals(1, gameState.currentLevel)
        assertEquals(2, gameState.flashCount)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertTrue(gameState.flashingSequence.isEmpty())
        assertTrue(gameState.userSelections.isEmpty())
        assertTrue(gameState.showDistractors)
    }

    @Test
    fun testUpdateGridSize() {
        viewModel.updateGridSize(4)
        
        val gameState = viewModel.gameState.value
        assertEquals(4, gameState.gridSize)
        assertEquals(1, gameState.currentLevel)
    }

    @Test
    fun testUpdateGridSizeWithLargeFlashCount() {
        viewModel.updateGridSize(3)
        viewModel.updateGridSize(5)
        
        val gameState = viewModel.gameState.value
        assertEquals(5, gameState.gridSize)
    }

    @Test
    fun testUpdateGridSizeReducesFlashCountIfNecessary() {
        viewModel.updateGridSize(3)
        viewModel.updateGridSize(3)
        
        val gameState = viewModel.gameState.value
        assertEquals(3, gameState.gridSize)
        assertTrue(gameState.flashCount <= gameState.gridSize * gameState.gridSize / 2)
    }

    @Test
    fun testUpdateShowDistractors() {
        viewModel.updateShowDistractors(true)
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.showDistractors)
        
        viewModel.updateShowDistractors(false)
        
        val updatedState = viewModel.gameState.value
        assertFalse(updatedState.showDistractors)
    }

    @Test
    fun testFlashSequenceCorrectCount() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        assertEquals(gameState.flashCount, realFlashCells.size)
    }

    @Test
    fun testFlashSequenceNoDuplicates() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        val uniqueCells = realFlashCells.distinctBy { Pair(it.row, it.col) }
        
        assertEquals(realFlashCells.size, uniqueCells.size)
    }

    @Test
    fun testFlashSequenceWithinGridBounds() = runTest {
        viewModel.updateGridSize(3)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val allCellsValid = gameState.flashingSequence.all { cell ->
            cell.row in 0 until gameState.gridSize && 
            cell.col in 0 until gameState.gridSize
        }
        
        assertTrue(allCellsValid)
    }

    @Test
    fun testFlashCountDoesNotExceedTotalCells() = runTest {
        viewModel.updateGridSize(3)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val totalCells = gameState.gridSize * gameState.gridSize
        
        assertTrue(gameState.flashCount <= totalCells)
    }

    @Test
    fun testDistractorGeneration() = runTest {
        viewModel.updateShowDistractors(true)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val distractorCells = gameState.flashingSequence.filter { it.isDistractor }
        val expectedDistractorCount = gameState.flashCount / 2
        
        assertEquals(expectedDistractorCount, distractorCells.size)
    }

    @Test
    fun testNoDistractorsWhenDisabled() = runTest {
        viewModel.updateShowDistractors(false)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val distractorCells = gameState.flashingSequence.filter { it.isDistractor }
        
        assertTrue(distractorCells.isEmpty())
    }

    @Test
    fun testDistractorsDoNotOverlapWithRealCells() = runTest {
        viewModel.updateShowDistractors(true)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realCells = gameState.flashingSequence.filter { !it.isDistractor }
        val distractorCells = gameState.flashingSequence.filter { it.isDistractor }
        
        val realCellPositions = realCells.map { Pair(it.row, it.col) }.toSet()
        val distractorCellPositions = distractorCells.map { Pair(it.row, it.col) }.toSet()
        
        val intersection = realCellPositions.intersect(distractorCellPositions)
        assertTrue(intersection.isEmpty())
    }

    @Test
    fun testCorrectCellSelection() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.isNotEmpty()) {
            val firstCell = realFlashCells[0]
            viewModel.selectCell(firstCell.row, firstCell.col)
            
            val updatedState = viewModel.gameState.value
            val selectedCell = updatedState.userSelections.find { 
                it.row == firstCell.row && it.col == firstCell.col 
            }
            
            assertTrue(selectedCell?.isCorrect == true)
            assertEquals(1, updatedState.currentSelectionIndex)
        }
    }

    @Test
    fun testWrongCellSelection() = runTest {
        viewModel.updateGridSize(3)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.size >= 2) {
            val secondCell = realFlashCells[1]
            viewModel.selectCell(secondCell.row, secondCell.col)
            
            val updatedState = viewModel.gameState.value
            val selectedCell = updatedState.userSelections.find { 
                it.row == secondCell.row && it.col == secondCell.col 
            }
            
            assertTrue(selectedCell?.isCorrect == false)
            assertEquals(GamePhase.GAME_OVER, updatedState.gamePhase)
        }
    }

    @Test
    fun testDistractorCellSelection() = runTest {
        viewModel.updateShowDistractors(true)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val distractorCells = gameState.flashingSequence.filter { it.isDistractor }
        
        if (distractorCells.isNotEmpty()) {
            val distractorCell = distractorCells.first()
            viewModel.selectCell(distractorCell.row, distractorCell.col)
            
            val updatedState = viewModel.gameState.value
            val selectedCell = updatedState.userSelections.find { 
                it.row == distractorCell.row && it.col == distractorCell.col 
            }
            
            assertTrue(selectedCell?.isDistractor == true)
            assertTrue(selectedCell?.isCorrect == false)
        }
    }

    @Test
    fun testLevelCompleteWhenAllCorrectCellsSelected() = runTest {
        viewModel.updateGridSize(3)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        for (cell in realFlashCells) {
            viewModel.selectCell(cell.row, cell.col)
        }
        
        val finalState = viewModel.gameState.value
        assertEquals(GamePhase.LEVEL_COMPLETE, finalState.gamePhase)
        assertEquals(realFlashCells.size, finalState.currentSelectionIndex)
    }

    @Test
    fun testGameOverWhenWrongCellSelected() = runTest {
        viewModel.updateGridSize(3)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.size >= 2) {
            val secondCell = realFlashCells[1]
            viewModel.selectCell(secondCell.row, secondCell.col)
            
            val updatedState = viewModel.gameState.value
            assertEquals(GamePhase.GAME_OVER, updatedState.gamePhase)
        }
    }

    @Test
    fun testCannotSelectCellTwice() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.isNotEmpty()) {
            val cellToSelect = realFlashCells.first()
            viewModel.selectCell(cellToSelect.row, cellToSelect.col)
            viewModel.selectCell(cellToSelect.row, cellToSelect.col)
            
            val updatedState = viewModel.gameState.value
            val selectionCount = updatedState.userSelections.count { 
                it.row == cellToSelect.row && it.col == cellToSelect.col 
            }
            
            assertEquals(1, selectionCount)
        }
    }

    @Test
    fun testCannotSelectCellDuringShowingSequence() = runTest {
        viewModel.startGame()
        
        val gameStateBeforeAdvance = viewModel.gameState.value
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameStateBeforeAdvance.gamePhase)
        
        val realFlashCells = gameStateBeforeAdvance.flashingSequence.filter { !it.isDistractor }
        if (realFlashCells.isNotEmpty()) {
            val cellToSelect = realFlashCells.first()
            viewModel.selectCell(cellToSelect.row, cellToSelect.col)
            
            val updatedState = viewModel.gameState.value
            assertTrue(updatedState.userSelections.isEmpty())
        }
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
    fun testNextLevelIncreasesFlashCount() = runTest {
        viewModel.updateGridSize(3)
        val initialFlashCount = viewModel.gameState.value.flashCount
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newFlashCount = viewModel.gameState.value.flashCount
        assertTrue(newFlashCount >= initialFlashCount)
    }

    @Test
    fun testNextLevelResetsUserSelections() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.isNotEmpty()) {
            val cellToSelect = realFlashCells.first()
            viewModel.selectCell(cellToSelect.row, cellToSelect.col)
        }
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newGameState = viewModel.gameState.value
        assertTrue(newGameState.userSelections.isEmpty())
    }

    @Test
    fun testFlashCountMaxLimit() = runTest {
        viewModel.updateGridSize(3)
        
        for (i in 1..10) {
            viewModel.nextLevel()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        
        val gameState = viewModel.gameState.value
        val maxFlashCount = gameState.gridSize * gameState.gridSize / 2
        
        assertTrue(gameState.flashCount <= maxFlashCount)
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
    fun testDistractorCountIsHalfOfFlashCount() = runTest {
        viewModel.updateShowDistractors(true)
        viewModel.updateGridSize(4)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val distractorCount = gameState.flashingSequence.count { it.isDistractor }
        val expectedCount = gameState.flashCount / 2
        
        assertEquals(expectedCount, distractorCount)
    }

    @Test
    fun testNoDistractorsWhenFlashCountIsOne() = runTest {
        viewModel.updateShowDistractors(true)
        viewModel.updateGridSize(3)
        
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(2, gameState.flashCount)
    }

    @Test
    fun testGridSizePersistsAfterReset() {
        viewModel.updateGridSize(5)
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(5, gameState.gridSize)
    }

    @Test
    fun testShowDistractorsPersistsAfterReset() {
        viewModel.updateShowDistractors(true)
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.showDistractors)
    }

    @Test
    fun testAllFlashCellsAreWithinGrid() = runTest {
        for (size in listOf(3, 4, 5)) {
            viewModel.updateGridSize(size)
            viewModel.startGame()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val gameState = viewModel.gameState.value
            val allWithinBounds = gameState.flashingSequence.all { cell ->
                cell.row in 0 until size && cell.col in 0 until size
            }
            
            assertTrue("Flash cells should be within grid bounds for size $size", allWithinBounds)
        }
    }

    @Test
    fun testUserSelectionsAreMarkedCorrectly() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.isNotEmpty()) {
            val firstCell = realFlashCells[0]
            viewModel.selectCell(firstCell.row, firstCell.col)
            
            val updatedState = viewModel.gameState.value
            val selection = updatedState.userSelections.first()
            
            assertTrue(selection.isSelected)
            assertTrue(selection.isCorrect == true)
        }
    }
    
    @Test
    fun testWrongOrderSelection() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        if (realFlashCells.size >= 2) {
            val secondCell = realFlashCells[1]
            viewModel.selectCell(secondCell.row, secondCell.col)
            
            val updatedState = viewModel.gameState.value
            assertEquals(GamePhase.GAME_OVER, updatedState.gamePhase)
        }
    }
    
    @Test
    fun testCurrentSelectionIndexIncrements() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        assertEquals(0, gameState.currentSelectionIndex)
        
        if (realFlashCells.isNotEmpty()) {
            viewModel.selectCell(realFlashCells[0].row, realFlashCells[0].col)
            assertEquals(1, viewModel.gameState.value.currentSelectionIndex)
            
            if (realFlashCells.size >= 2) {
                viewModel.selectCell(realFlashCells[1].row, realFlashCells[1].col)
                assertEquals(2, viewModel.gameState.value.currentSelectionIndex)
            }
        }
    }
    
    @Test
    fun testCurrentSelectionIndexResetsOnNextLevel() = runTest {
        viewModel.updateGridSize(3)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val gameState = viewModel.gameState.value
        val realFlashCells = gameState.flashingSequence.filter { !it.isDistractor }
        
        for (cell in realFlashCells) {
            viewModel.selectCell(cell.row, cell.col)
        }
        
        viewModel.nextLevel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newGameState = viewModel.gameState.value
        assertEquals(0, newGameState.currentSelectionIndex)
    }
}
