package top.alan.memorush.model

data class MissingItemsGameState(
    val currentLevel: Int = 1,
    val totalItems: Int = 5,
    val missingCount: Int = 1,
    val items: List<SceneItem> = emptyList(),
    val missingItems: List<SceneItem> = emptyList(),
    val foundItems: List<SceneItem> = emptyList(),
    val gamePhase: GamePhase = GamePhase.IDLE,
    val score: Int = 0,
    val observationTime: Long = 5000,
    val gridSize: Int = 3,
    val showHint: Boolean = false,
    val attemptsRemaining: Int = 3
)
