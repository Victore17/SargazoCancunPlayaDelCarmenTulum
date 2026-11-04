package victor.android.sargazocancnplayadelcarmentulum.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.ui.components.PlaceholderScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Detalle de una playa. Se construye en la etapa 4. */
@Composable
fun BeachDetailScreen(
    beachId: Long,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        titleRes = R.string.screen_beach_detail_title,
        message = stringResource(R.string.beach_detail_id, beachId),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun BeachDetailScreenPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme { BeachDetailScreen(beachId = 1) }
}
