package top.alan.memorush.model

import androidx.compose.ui.graphics.Color

enum class SimonColor(
    val displayName: String,
    val color: Color,
    val highlightColor: Color
) {
    RED("红色", Color(0xFFC62828), Color(0xFFFF5252)),
    BLUE("蓝色", Color(0xFF1565C0), Color(0xFF448AFF)),
    GREEN("绿色", Color(0xFF2E7D32), Color(0xFF69F0AE)),
    YELLOW("黄色", Color(0xFFF9A825), Color(0xFFFFFF00)),
    PURPLE("紫色", Color(0xFF6A1B9A), Color(0xFFEA80FC)),
    ORANGE("橙色", Color(0xFFEF6C00), Color(0xFFFFAB40)),
    CYAN("青色", Color(0xFF00838F), Color(0xFF18FFFF)),
    PINK("粉色", Color(0xFFAD1457), Color(0xFFFF4081)),
    INDIGO("靛蓝", Color(0xFF283593), Color(0xFF8C9EFF))
}
