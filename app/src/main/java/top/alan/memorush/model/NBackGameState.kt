package top.alan.memorush.model

data class NBackGameState(
    val nLevel: Int = 2,
    val currentLevel: Int = 1,
    val totalTrials: Int = 20,
    val currentTrial: Int = 0,
    val stimulusSequence: List<NBackStimulus> = emptyList(),
    val currentStimulus: NBackStimulus? = null,
    val gamePhase: GamePhase = GamePhase.IDLE,
    val score: Int = 0,
    val correctResponses: Int = 0,
    val totalResponses: Int = 0,
    val showFeedback: Boolean = false,
    val lastResponseCorrect: Boolean? = null,
    val stimulusMode: StimulusMode = StimulusMode.LETTER,
    val stimulusDuration: Long = 2000,
    val isDualMode: Boolean = false,
    val positionSequence: List<Int> = emptyList(),
    val currentPosition: Int? = null
)

enum class StimulusMode {
    LETTER,
    POSITION,
    DUAL
}
