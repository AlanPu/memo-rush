package top.alan.memorush.model

import androidx.compose.ui.graphics.Color

enum class StroopColor(
    val displayName: String,
    val color: Color
) {
    RED("红", Color(0xFFE53935)),
    BLUE("蓝", Color(0xFF1E88E5)),
    GREEN("绿", Color(0xFF43A047)),
    YELLOW("黄", Color(0xFFFDD835)),
    PURPLE("紫", Color(0xFF8E24AA)),
    ORANGE("橙", Color(0xFFFB8C00))
}

data class StroopTrial(
    val wordColor: StroopColor,
    val wordMeaning: StroopColor,
    val isCongruent: Boolean = wordColor == wordMeaning
)

data class StroopTaskGameState(
    val currentLevel: Int = 1,
    val currentTrial: Int = 0,
    val totalTrials: Int = 10,
    val trial: StroopTrial? = null,
    val gamePhase: GamePhase = GamePhase.IDLE,
    val score: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val timeLimit: Long = 3000,
    val remainingTime: Long = timeLimit,
    val showFeedback: Boolean = false,
    val lastAnswerCorrect: Boolean = false,
    val congruentRatio: Float = 0.5f,
    val streakCount: Int = 0
)
