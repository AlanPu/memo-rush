package top.alan.memorush.model

import androidx.compose.ui.graphics.Color

enum class NBackStimulus(
    val displayName: String,
    val letter: String,
    val color: Color
) {
    A("A", "A", Color(0xFFE53935)),
    B("B", "B", Color(0xFF1E88E5)),
    C("C", "C", Color(0xFF43A047)),
    D("D", "D", Color(0xFFFB8C00)),
    E("E", "E", Color(0xFF8E24AA)),
    F("F", "F", Color(0xFF00ACC1)),
    G("G", "G", Color(0xFF3949AB)),
    H("H", "H", Color(0xFFD81B60));

    companion object {
        fun random(): NBackStimulus = entries.random()
    }
}
