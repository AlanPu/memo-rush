package top.alan.memorush.model

data class GridCell(
    val row: Int,
    val col: Int,
    val isFlashing: Boolean = false,
    val isSelected: Boolean = false,
    val isCorrect: Boolean? = null,
    val isDistractor: Boolean = false
)
