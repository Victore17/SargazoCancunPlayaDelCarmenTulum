package victor.android.sargazocancnplayadelcarmentulum.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.ui.components.PlaceholderScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Ajustes de notificaciones. Se construye en la etapa 7. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(titleRes = R.string.screen_settings_title, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme { SettingsScreen() }
}
