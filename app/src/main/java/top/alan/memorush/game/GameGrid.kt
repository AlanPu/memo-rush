package top.alan.memorush.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.alan.memorush.model.GridCell

@Composable
fun GameGrid(
    gridSize: Int,
    cells: List<GridCell>,
    enabled: Boolean = true,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until gridSize) {
                    val cell = cells.find { it.row == row && it.col == col }
                        ?: GridCell(row, col)
                    
                    GridCellComposable(
                        cell = cell,
                        enabled = enabled,
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}
