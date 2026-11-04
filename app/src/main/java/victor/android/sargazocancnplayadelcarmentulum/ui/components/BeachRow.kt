package victor.android.sargazocancnplayadelcarmentulum.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.labelRes

/** Fila de una playa: color, nombre, nivel y estrella para agregar o quitar de favoritas. */
@Composable
fun BeachRow(
    status: BeachStatus,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val beach = status.beach
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LevelDot(level = status.level, size = 24.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = beach.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(status.level.labelRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (beach.location == null) {
                Text(
                    text = stringResource(R.string.beach_without_location),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                painter = painterResource(
                    if (beach.isFavorite) R.drawable.ic_nav_favorite else R.drawable.ic_favorite_border,
                ),
                contentDescription = stringResource(
                    if (beach.isFavorite) R.string.detail_favorite_remove else R.string.detail_favorite_add,
                ),
                tint = if (beach.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BeachRowPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        Column {
            BeachRow(
                status = previewStatus("Playa Delfines", SargassumLevel.EXCESSIVE, GeoPoint(21.05, -86.78), true),
                onClick = {},
                onToggleFavorite = {},
            )
            BeachRow(
                status = previewStatus("Playa nueva", SargassumLevel.LOW, null, false),
                onClick = {},
                onToggleFavorite = {},
            )
        }
    }
}

private fun previewStatus(name: String, level: SargassumLevel, location: GeoPoint?, favorite: Boolean) =
    BeachStatus(
        beach = Beach(
            id = 1,
            name = name,
            location = location,
            imagePosition = null,
            isFavorite = favorite,
            notificationsEnabled = true,
        ),
        level = level,
        date = null,
    )
