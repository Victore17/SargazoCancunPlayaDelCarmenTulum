package victor.android.sargazocancnplayadelcarmentulum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.color
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.labelRes

/** Círculo del color del nivel, con borde blanco como en el semáforo oficial. */
@Composable
fun LevelDot(level: SargassumLevel, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = level.color, shape = CircleShape)
            .border(width = 2.dp, color = Color.White, shape = CircleShape),
    )
}

/** Círculo de color y nombre del nivel. */
@Composable
fun LevelLabel(level: SargassumLevel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LevelDot(level)
        Text(text = stringResource(level.labelRes), style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun LevelLabelPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        Column {
            SargassumLevel.entries.forEach { LevelLabel(it) }
        }
    }
}
