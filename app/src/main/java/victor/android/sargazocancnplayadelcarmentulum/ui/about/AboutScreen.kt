package victor.android.sargazocancnplayadelcarmentulum.ui.about

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/**
 * Información de la app: fuente de los datos, aviso de que no es oficial y créditos.
 * Es contenido fijo, sin estado, por eso no necesita ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
            AboutSection(R.string.about_section_data, R.string.data_source)
            AboutSection(R.string.about_section_notice, R.string.unofficial_disclaimer)
            AboutSection(R.string.about_section_weather, R.string.about_weather_credit)
            AboutSection(R.string.about_section_map, R.string.about_map_credit)
        }
    }
}

@Composable
private fun AboutSection(@StringRes titleRes: Int, @StringRes textRes: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme { AboutScreen(onBack = {}) }
}
