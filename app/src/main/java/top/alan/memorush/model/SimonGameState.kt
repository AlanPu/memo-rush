package top.alan.memorush.model

data class SimonGameState(
    val colorCount: Int = 4,
    val currentLevel: Int = 1,
    val sequenceLength: Int = 2,
    val colorSequence: List<SimonColor> = emptyList(),
    val userSequence: List<SimonColor> = emptyList(),
    val gamePhase: GamePhase = GamePhase.IDLE,
    val reverseMode: Boolean = false,
    val currentInputIndex: Int = 0,
    val currentHighlightColor: SimonColor? = null
)
