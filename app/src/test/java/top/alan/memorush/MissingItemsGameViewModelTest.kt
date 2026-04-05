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
import top.alan.memorush.game.MissingItemsGameViewModel
import top.alan.memorush.model.GamePhase

@OptIn(ExperimentalCoroutinesApi::class)
class MissingItemsGameViewModelTest {

    private lateinit var viewModel: MissingItemsGameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MissingItemsGameViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGameInitialState() {
        val gameState = viewModel.gameState.value
        
        assertEquals(1, gameState.currentLevel)
        assertEquals(5, gameState.totalItems)
        assertEquals(1, gameState.missingCount)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertEquals(0, gameState.score)
        assertEquals(5000, gameState.observationTime)
        assertEquals(3, gameState.gridSize)
        assertFalse(gameState.showHint)
        assertEquals(3, gameState.attemptsRemaining)
    }

    @Test
    fun testGameReset() {
        viewModel.setTotalItems(10)
        viewModel.setMissingCount(3)
        viewModel.setObservationTime(8000)
        
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(5, gameState.totalItems)
        assertEquals(1, gameState.missingCount)
        assertEquals(5000, gameState.observationTime)
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
    }

    @Test
    fun testSetTotalItems() {
        viewModel.setTotalItems(8)
        
        val gameState = viewModel.gameState.value
        assertEquals(8, gameState.totalItems)
    }

    @Test
    fun testSetMissingCount() {
        viewModel.setMissingCount(3)
        
        val gameState = viewModel.gameState.value
        assertEquals(3, gameState.missingCount)
    }

    @Test
    fun testSetObservationTime() {
        viewModel.setObservationTime(7000)
        
        val gameState = viewModel.gameState.value
        assertEquals(7000, gameState.observationTime)
    }

    @Test
    fun testStartGameGeneratesItems() = runTest {
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(GamePhase.SHOWING_SEQUENCE, gameState.gamePhase)
        assertEquals(5, gameState.items.size)
        assertTrue(gameState.missingItems.size == 1)
    }

    @Test
    fun testStartGameWithCustomSettings() = runTest {
        viewModel.setTotalItems(8)
        viewModel.setMissingCount(2)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        
        assertEquals(8, gameState.items.size)
        assertEquals(2, gameState.missingItems.size)
    }

    @Test
    fun testObservationPhaseTransitionsToUserInput() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.startGame()
        
        assertEquals(GamePhase.SHOWING_SEQUENCE, viewModel.gameState.value.gamePhase)
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.USER_INPUT, gameState.gamePhase)
    }

    @Test
    fun testItemsRemovedAfterObservation() = runTest {
        viewModel.setTotalItems(5)
        viewModel.setMissingCount(2)
        viewModel.setObservationTime(2000)
        viewModel.startGame()
        
        val initialItemCount = viewModel.gameState.value.items.size
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val gameState = viewModel.gameState.value
        assertEquals(initialItemCount - 2, gameState.items.size)
    }

    @Test
    fun testCorrectPositionClick() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val gameState = viewModel.gameState.value
        val missingItem = gameState.missingItems.first()
        
        val scoreBefore = gameState.score
        viewModel.onPositionClick(missingItem.position.x, missingItem.position.y)
        
        val updatedState = viewModel.gameState.value
        assertTrue(updatedState.foundItems.contains(missingItem))
        assertEquals(scoreBefore + 10, updatedState.score)
    }

    @Test
    fun testWrongPositionClick() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val gameState = viewModel.gameState.value
        val missingItem = gameState.missingItems.first()
        
        viewModel.onPositionClick(missingItem.position.x + 200, missingItem.position.y + 200)
        
        val updatedState = viewModel.gameState.value
        assertEquals(2, updatedState.attemptsRemaining)
        assertTrue(updatedState.foundItems.isEmpty())
    }

    @Test
    fun testGameOverAfterThreeWrongAttempts() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.setTotalItems(5)
        viewModel.setMissingCount(1)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        for (i in 0 until 3) {
            if (viewModel.gameState.value.gamePhase == GamePhase.USER_INPUT) {
                viewModel.onPositionClick(1000 + i * 100, 1000 + i * 100)
            }
        }
        
        val finalState = viewModel.gameState.value
        assertEquals(GamePhase.GAME_OVER, finalState.gamePhase)
    }

    @Test
    fun testLevelCompleteWhenAllItemsFound() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.setTotalItems(5)
        viewModel.setMissingCount(1)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val gameState = viewModel.gameState.value
        val missingItem = gameState.missingItems.first()
        
        viewModel.onPositionClick(missingItem.position.x, missingItem.position.y)
        
        val updatedState = viewModel.gameState.value
        assertEquals(GamePhase.LEVEL_COMPLETE, updatedState.gamePhase)
    }

    @Test
    fun testNextLevelIncreasesDifficulty() {
        val initialLevel = viewModel.gameState.value.currentLevel
        val initialTotalItems = viewModel.gameState.value.totalItems
        
        viewModel.nextLevel()
        
        val gameState = viewModel.gameState.value
        assertEquals(initialLevel + 1, gameState.currentLevel)
        assertEquals(initialTotalItems + 1, gameState.totalItems)
    }

    @Test
    fun testNextLevelDecreasesObservationTime() {
        val initialTime = viewModel.gameState.value.observationTime
        
        viewModel.nextLevel()
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.observationTime < initialTime)
    }

    @Test
    fun testNextLevelIncreasesMissingCountEveryThreeLevels() {
        viewModel.nextLevel()
        assertEquals(1, viewModel.gameState.value.missingCount)
        
        viewModel.nextLevel()
        assertEquals(2, viewModel.gameState.value.missingCount)
        
        viewModel.nextLevel()
        assertEquals(2, viewModel.gameState.value.missingCount)
        
        viewModel.nextLevel()
        viewModel.nextLevel()
        assertEquals(3, viewModel.gameState.value.missingCount)
    }

    @Test
    fun testShowHint() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        viewModel.showHint()
        
        val gameState = viewModel.gameState.value
        assertTrue(gameState.showHint)
    }

    @Test
    fun testCannotChangeSettingsDuringGame() = runTest {
        viewModel.startGame()
        
        viewModel.setTotalItems(10)
        viewModel.setMissingCount(5)
        viewModel.setObservationTime(10000)
        
        val gameState = viewModel.gameState.value
        assertEquals(5, gameState.totalItems)
        assertEquals(1, gameState.missingCount)
        assertEquals(5000, gameState.observationTime)
    }

    @Test
    fun testTotalItemsMaxLimit() {
        viewModel.setTotalItems(15)
        
        val gameState = viewModel.gameState.value
        assertEquals(12, gameState.totalItems)
    }

    @Test
    fun testMissingCountMaxLimit() {
        viewModel.setMissingCount(10)
        
        val gameState = viewModel.gameState.value
        assertEquals(4, gameState.missingCount)
    }

    @Test
    fun testObservationTimeMinLimit() {
        viewModel.setObservationTime(1000)
        
        val gameState = viewModel.gameState.value
        assertEquals(2000, gameState.observationTime)
    }

    @Test
    fun testFoundItemsCountCorrect() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.setTotalItems(5)
        viewModel.setMissingCount(2)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val missingItems = viewModel.gameState.value.missingItems
        
        viewModel.onPositionClick(missingItems[0].position.x, missingItems[0].position.y)
        assertEquals(1, viewModel.gameState.value.foundItems.size)
        
        viewModel.onPositionClick(missingItems[1].position.x, missingItems[1].position.y)
        assertEquals(2, viewModel.gameState.value.foundItems.size)
    }

    @Test
    fun testScoreIncrementsOnCorrectFind() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val scoreBefore = viewModel.gameState.value.score
        val missingItem = viewModel.gameState.value.missingItems.first()
        
        viewModel.onPositionClick(missingItem.position.x, missingItem.position.y)
        
        val scoreAfter = viewModel.gameState.value.score
        assertEquals(scoreBefore + 10, scoreAfter)
    }

    @Test
    fun testMultipleCorrectFinds() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.setTotalItems(6)
        viewModel.setMissingCount(3)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val missingItems = viewModel.gameState.value.missingItems
        
        for (item in missingItems) {
            viewModel.onPositionClick(item.position.x, item.position.y)
        }
        
        val gameState = viewModel.gameState.value
        assertEquals(30, gameState.score)
        assertEquals(GamePhase.LEVEL_COMPLETE, gameState.gamePhase)
    }

    @Test
    fun testItemsHaveUniqueIds() = runTest {
        viewModel.setTotalItems(8)
        viewModel.startGame()
        
        val gameState = viewModel.gameState.value
        val ids = gameState.items.map { it.id }
        val uniqueIds = ids.toSet()
        
        assertEquals(ids.size, uniqueIds.size)
    }

    @Test
    fun testGameResetsToIdle() {
        viewModel.resetGame()
        
        val gameState = viewModel.gameState.value
        assertEquals(GamePhase.IDLE, gameState.gamePhase)
        assertEquals(1, gameState.currentLevel)
        assertEquals(0, gameState.score)
    }

    @Test
    fun testFoundItemReappearsOnScreen() = runTest {
        viewModel.setObservationTime(2000)
        viewModel.setTotalItems(5)
        viewModel.setMissingCount(1)
        viewModel.startGame()
        
        testDispatcher.scheduler.advanceTimeBy(2500)
        
        val gameState = viewModel.gameState.value
        val missingItem = gameState.missingItems.first()
        
        val itemsBefore = gameState.items.size
        
        viewModel.onPositionClick(missingItem.position.x, missingItem.position.y)
        
        val updatedState = viewModel.gameState.value
        assertEquals(itemsBefore + 1, updatedState.items.size)
        assertTrue(updatedState.items.contains(missingItem))
    }
}
