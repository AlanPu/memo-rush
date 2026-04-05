package top.alan.memorush.model

enum class GameType(
    val displayName: String,
    val description: String
) {
    GRID_MEMORY(
        displayName = "网格翻牌",
        description = "记住卡片位置，找出所有配对"
    ),
    SIMON_SAYS(
        displayName = "西蒙说",
        description = "记住并重复颜色序列"
    ),
    N_BACK(
        displayName = "N-Back任务",
        description = "判断当前项目是否与N步前相同"
    ),
    MISSING_ITEMS(
        displayName = "消失的物品",
        description = "观察场景变化，找出消失的物品"
    ),
    STROOP_TASK(
        displayName = "斯特鲁普任务",
        description = "说出文字颜色而非文字内容"
    )
}
