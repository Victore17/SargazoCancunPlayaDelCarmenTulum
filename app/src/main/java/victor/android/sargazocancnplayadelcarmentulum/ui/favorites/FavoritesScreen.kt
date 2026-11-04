package victor.android.sargazocancnplayadelcarmentulum.ui.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.ui.components.PlaceholderScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Playas favoritas. Se construye en la etapa 5. */
@Composable
fun FavoritesScreen(
    @Suppress("UNUSED_PARAMETER") onBeachClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(titleRes = R.string.screen_favorites_title, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme { FavoritesScreen(onBeachClick = {}) }
}
