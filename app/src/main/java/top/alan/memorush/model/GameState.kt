package top.alan.memorush.model

data class GameState(
    val gridSize: Int = 3,
    val currentLevel: Int = 1,
    val flashCount: Int = 2,
    val flashingSequence: List<GridCell> = emptyList(),
    val userSelections: List<GridCell> = emptyList(),
    val gamePhase: GamePhase = GamePhase.IDLE,
    val showDistractors: Boolean = false,
    val currentSelectionIndex: Int = 0
)
