package victor.android.sargazocancnplayadelcarmentulum.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.ui.components.LevelDot
import victor.android.sargazocancnplayadelcarmentulum.ui.components.LevelLabel
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme
import victor.android.sargazocancnplayadelcarmentulum.ui.util.formatLong
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Pantalla oculta de administración (se abre con 7 toques en "Ajustes"). */
@Composable
fun AdminScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onImagePicked(it.toString()) }
    }

    val message = uiState.message?.let { stringResource(it.textRes) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        AdminContent(
            uiState = uiState,
            actions = AdminActions(
                onSignIn = { viewModel.signInWithGoogle(context) },
                onSignOut = viewModel::signOut,
                onPickImage = {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onLevelChange = viewModel::onLevelChange,
                onDateChange = viewModel::onDateChange,
                onPublish = viewModel::publish,
                onStartOver = viewModel::startOver,
            ),
            modifier = Modifier.padding(padding),
        )
    }
}

/** Acciones de la pantalla, agrupadas para no pasar muchos parámetros sueltos. */
data class AdminActions(
    val onSignIn: () -> Unit = {},
    val onSignOut: () -> Unit = {},
    val onPickImage: () -> Unit = {},
    val onLevelChange: (String, SargassumLevel) -> Unit = { _, _ -> },
    val onDateChange: (LocalDate) -> Unit = {},
    val onPublish: () -> Unit = {},
    val onStartOver: () -> Unit = {},
)

@Composable
private fun AdminContent(
    uiState: AdminUiState,
    actions: AdminActions,
    modifier: Modifier = Modifier,
) {
    val session = uiState.session
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = stringResource(R.string.admin_title), style = MaterialTheme.typography.headlineSmall)
        }
        when (session) {
            AdminSessionUi.Checking -> item { CenteredProgress(R.string.admin_checking_session) }
            AdminSessionUi.SignedOut -> item { LoginForm(uiState, actions) }
            is AdminSessionUi.NotAdmin -> item {
                SessionCard(email = session.email, onSignOut = actions.onSignOut) {
                    Text(stringResource(R.string.admin_not_admin), color = MaterialTheme.colorScheme.error)
                }
            }
            is AdminSessionUi.Admin -> {
                item { SessionCard(email = session.email, onSignOut = actions.onSignOut) }
                adminSteps(uiState.step, actions)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.adminSteps(step: AdminStep, actions: AdminActions) {
    when (step) {
        AdminStep.PickImage -> item { PickImageCard(onPickImage = actions.onPickImage) }
        AdminStep.Processing -> item { CenteredProgress(R.string.admin_processing) }
        is AdminStep.Published -> item { PublishedCard(step, onStartOver = actions.onStartOver) }
        is AdminStep.Review -> {
            item { ReviewHeader(step, actions) }
            if (step.needsAttention) item { AttentionCard(step) }
            // Primero lo que necesita revisión, para que no se pase por alto al final de la lista.
            val (toReview, ok) = step.rows.partition { it.needsReview }
            if (toReview.isNotEmpty()) {
                item { SectionTitle(R.string.admin_section_attention) }
                items(toReview, key = { "r-" + it.name }) { row -> ReviewRowItem(row, actions.onLevelChange) }
            }
            item { SectionTitle(R.string.admin_section_ok) }
            items(ok, key = { "o-" + it.name }) { row -> ReviewRowItem(row, actions.onLevelChange) }
            item {
                PublishButton(
                    needsAttention = step.needsAttention,
                    isPublishing = step.isPublishing,
                    onPublish = actions.onPublish,
                )
            }
        }
    }
}

@Composable
private fun LoginForm(uiState: AdminUiState, actions: AdminActions) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.admin_sign_in_intro))
            Button(
                onClick = actions.onSignIn,
                enabled = !uiState.isSigningIn,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.heightIn(max = 20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.admin_sign_in_google))
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    email: String,
    onSignOut: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.admin_signed_in_as, email),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onSignOut) { Text(stringResource(R.string.admin_sign_out)) }
            }
            extra()
        }
    }
}

@Composable
private fun PickImageCard(onPickImage: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.admin_pick_intro))
            Button(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.admin_pick_image))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewHeader(review: AdminStep.Review, actions: AdminActions) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val imageDescription = stringResource(R.string.admin_selected_image)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.admin_review_title), style = MaterialTheme.typography.titleMedium)
            AsyncImage(
                model = review.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .semantics { contentDescription = imageDescription },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.admin_date, review.date.formatLong()),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showDatePicker = true }) { Text(stringResource(R.string.admin_change_date)) }
            }
            if (!review.dateDetected) {
                Text(stringResource(R.string.admin_date_not_detected), color = MaterialTheme.colorScheme.error)
            }
            Text(pluralStringResource(R.plurals.admin_review_summary, review.detectedCount, review.detectedCount))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = review.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        actions.onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.admin_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.admin_cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/** Resumen de lo que hay que revisar: nombres nuevos, playas que no aparecieron y círculos sin nombre. */
@Composable
private fun AttentionCard(review: AdminStep.Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.admin_attention_title), style = MaterialTheme.typography.titleSmall)
            val newBeaches = review.newBeaches
            if (newBeaches.isNotEmpty()) {
                Text(
                    pluralStringResource(
                        R.plurals.admin_attention_new,
                        newBeaches.size,
                        newBeaches.size,
                        newBeaches.joinToString { it.name },
                    ),
                )
            }
            if (review.missingCount > 0) {
                Text(pluralStringResource(R.plurals.admin_attention_missing, review.missingCount, review.missingCount))
            }
            if (review.unlabeledDotCount > 0) {
                Text(
                    pluralStringResource(
                        R.plurals.admin_review_unlabeled,
                        review.unlabeledDotCount,
                        review.unlabeledDotCount,
                    ),
                )
            }
            Text(stringResource(R.string.admin_attention_hint), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionTitle(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 8.dp),
    )
}

/** Una playa en la revisión: al tocarla se elige otro nivel. */
@Composable
private fun ReviewRowItem(row: ReviewRow, onLevelChange: (String, SargassumLevel) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val changeDescription = stringResource(R.string.admin_change_level, row.name)
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (row.needsReview) {
                        Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    },
                )
                .clickable { expanded = true }
                .semantics { contentDescription = changeDescription }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LevelDot(row.level, size = 20.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.bodyLarge)
                if (row.isNewBeach) {
                    Text(
                        text = stringResource(R.string.admin_new_beach),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (row.isMissing) {
                    Text(
                        text = stringResource(R.string.admin_missing_beach),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            LevelLabel(row.level)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SargassumLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { LevelLabel(level) },
                    onClick = {
                        onLevelChange(row.name, level)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Si hay datos sin revisar, pide confirmación antes de publicar. */
@Composable
private fun PublishButton(needsAttention: Boolean, isPublishing: Boolean, onPublish: () -> Unit) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    Button(
        onClick = { if (needsAttention) showConfirm = true else onPublish() },
        enabled = !isPublishing,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(stringResource(if (isPublishing) R.string.admin_publishing else R.string.admin_publish))
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.admin_confirm_title)) },
            text = { Text(stringResource(R.string.admin_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onPublish()
                }) { Text(stringResource(R.string.admin_confirm_publish)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.admin_confirm_keep)) }
            },
        )
    }
}

@Composable
private fun PublishedCard(published: AdminStep.Published, onStartOver: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                pluralStringResource(
                    R.plurals.admin_published,
                    published.beachCount,
                    published.date.formatLong(),
                    published.beachCount,
                ),
            )
            OutlinedButton(onClick = onStartOver, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.admin_upload_another))
            }
        }
    }
}

@Composable
private fun CenteredProgress(@StringRes textRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(stringResource(textRes))
    }
}

@get:StringRes
private val AdminMessage.textRes: Int
    get() = when (this) {
        AdminMessage.INVALID_CREDENTIALS -> R.string.admin_error_invalid_credentials
        AdminMessage.NETWORK_ERROR -> R.string.admin_error_network
        AdminMessage.SIGN_IN_ERROR -> R.string.admin_error_sign_in
        AdminMessage.GOOGLE_NOT_CONFIGURED -> R.string.admin_error_google_not_configured
        AdminMessage.NO_GOOGLE_ACCOUNT -> R.string.admin_error_no_google_account
        AdminMessage.IMAGE_READ_ERROR -> R.string.admin_error_image_read
        AdminMessage.NOT_A_SEMAFORO -> R.string.admin_error_not_semaforo
        AdminMessage.NOTHING_TO_PUBLISH -> R.string.admin_error_nothing_to_publish
        AdminMessage.PUBLISH_ERROR -> R.string.admin_error_publish
    }

@Preview(showBackground = true)
@Composable
private fun AdminLoginPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        AdminContent(
            uiState = AdminUiState(session = AdminSessionUi.SignedOut),
            actions = AdminActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminReviewPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        AdminContent(
            uiState = AdminUiState(
                session = AdminSessionUi.Admin("admin@example.com"),
                step = AdminStep.Review(
                    imageUri = "",
                    date = LocalDate.of(2026, 11, 3),
                    dateDetected = true,
                    rows = listOf(
                        ReviewRow("Punta Molas", SargassumLevel.EXCESSIVE, detected = true, hasLocation = true),
                        ReviewRow("Playa Xhanan", SargassumLevel.ABUNDANT, detected = true, hasLocation = true),
                        ReviewRow("Playa Nueva", SargassumLevel.LOW, detected = true, hasLocation = false),
                        ReviewRow("Akumal", SargassumLevel.NO_DATA, detected = false, hasLocation = true),
                    ),
                    unlabeledDotCount = 1,
                ),
            ),
            actions = AdminActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminPublishedPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        AdminContent(
            uiState = AdminUiState(
                session = AdminSessionUi.Admin("admin@example.com"),
                step = AdminStep.Published(LocalDate.of(2026, 11, 3), beachCount = 100),
            ),
            actions = AdminActions(),
        )
    }
}
