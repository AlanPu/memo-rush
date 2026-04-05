package top.alan.memorush.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset

data class SceneItem(
    val id: Int,
    val name: String,
    val icon: ItemIcon,
    val color: Color,
    val position: IntOffset,
    val size: Int = 48
)

enum class ItemIcon(
    val displayName: String,
    val shape: ItemShape
) {
    STAR("星星", ItemShape.STAR),
    HEART("爱心", ItemShape.HEART),
    CIRCLE("圆形", ItemShape.CIRCLE),
    SQUARE("方形", ItemShape.SQUARE),
    TRIANGLE("三角形", ItemShape.TRIANGLE),
    DIAMOND("菱形", ItemShape.DIAMOND),
    HEXAGON("六边形", ItemShape.HEXAGON),
    PENTAGON("五边形", ItemShape.PENTAGON),
    CROSS("十字", ItemShape.CROSS),
    MOON("月亮", ItemShape.MOON)
}

enum class ItemShape {
    STAR,
    HEART,
    CIRCLE,
    SQUARE,
    TRIANGLE,
    DIAMOND,
    HEXAGON,
    PENTAGON,
    CROSS,
    MOON
}
