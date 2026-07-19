package at.ee.pkmsapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.ee.pkmsapp.data.CaptureEntity
import at.ee.pkmsapp.data.CaptureRepository
import at.ee.pkmsapp.data.CaptureStatus
import at.ee.pkmsapp.sync.CaptureSyncScheduler
import at.ee.pkmsapp.ui.theme.PkmsAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var sharedLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedLink = extractSharedLink(intent)
        enableEdgeToEdge()
        setContent {
            PkmsAppTheme {
                val context = LocalContext.current
                val repository = remember { AppGraph.repository(context) }
                val syncScheduler = remember { AppGraph.syncScheduler(context) }
                LaunchedEffect(Unit) {
                    syncScheduler.enqueueSync()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CaptureScreen(
                        repository = repository,
                        syncScheduler = syncScheduler,
                        sharedLink = sharedLink,
                        onSharedLinkConsumed = { sharedLink = null },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sharedLink = extractSharedLink(intent)
    }

    private fun extractSharedLink(intent: Intent?): String? {
        val text = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        }
        return text?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    }
}

@Composable
fun CaptureScreen(
    repository: CaptureRepository,
    syncScheduler: CaptureSyncScheduler,
    sharedLink: String?,
    onSharedLinkConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pendingCount by repository.pendingCount.observeAsState(0)
    val syncedCount by repository.syncedCount.observeAsState(0)
    val latestCaptures by repository.latestCaptures.observeAsState(emptyList())
    val categories = remember {
        context.resources.getStringArray(R.array.capture_categories).toList()
    }
    var noteText by rememberSaveable { mutableStateOf("") }
    var linkText by rememberSaveable { mutableStateOf("") }
    var selectedCategories by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var cameraImages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageUris = selectedImageUris + uris
    }
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            cameraImages = cameraImages + bitmap
        }
    }

    LaunchedEffect(sharedLink) {
        if (!sharedLink.isNullOrBlank()) {
            linkText = listOf(linkText, sharedLink)
                .filter { it.isNotBlank() }
                .joinToString("\n")
            onSharedLinkConsumed()
        }
    }

    val links = linkText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val canSave = noteText.isNotBlank() || links.isNotEmpty() || selectedImageUris.isNotEmpty() || cameraImages.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "PKMS Capture",
            style = MaterialTheme.typography.headlineMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SyncCountCard(label = "Pending", count = pendingCount, modifier = Modifier.weight(1f))
            SyncCountCard(label = "Synced", count = syncedCount, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Quick note") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = linkText,
            onValueChange = { linkText = it },
            label = { Text("Links") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.weight(1f),
            ) {
                Text("Images (${selectedImageUris.size})")
            }
            Button(
                onClick = { cameraCapture.launch(null) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Camera (${cameraImages.size})")
            }
        }
        Button(
            onClick = {
                scope.launch {
                    val saved = repository.saveCapture(
                        content = noteText,
                        links = links,
                        imageUris = selectedImageUris,
                        cameraImages = cameraImages,
                        categories = selectedCategories,
                    )
                    if (saved) {
                        noteText = ""
                        linkText = ""
                        selectedCategories = emptyList()
                        selectedImageUris = emptyList()
                        cameraImages = emptyList()
                        syncScheduler.enqueueSync()
                    }
                }
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save note")
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(categories, key = { it }) { category ->
                FilterChip(
                    selected = category in selectedCategories,
                    onClick = {
                        selectedCategories = if (category in selectedCategories) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    },
                    label = { Text(category) },
                )
            }
        }
        Text(
            text = "Recent captures",
            style = MaterialTheme.typography.titleMedium,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            latestCaptures.forEach { capture ->
                CaptureListItem(
                    capture = capture,
                    onDelete = {
                        scope.launch {
                            if (capture.status == CaptureStatus.Synced) {
                                repository.deleteLocalHistory(capture.id)
                            } else {
                                repository.deletePendingCapture(capture.id)
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SyncCountCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun CaptureListItem(
    capture: CaptureEntity,
    onDelete: () -> Unit,
) {
    val links = capture.links.toValues()
    val categories = capture.categories.toValues()
    val attachmentCount = capture.attachmentPaths.toValues().size
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = capture.content.ifBlank { "(no text)" },
                style = MaterialTheme.typography.bodyLarge,
            )
            if (links.isNotEmpty()) {
                Text(
                    text = "${links.size} link${if (links.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (attachmentCount > 0) {
                Text(
                    text = "$attachmentCount image${if (attachmentCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (categories.isNotEmpty()) {
                Text(
                    text = categories.joinToString(" ") { "#$it" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = capture.statusLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun String?.toValues(): List<String> =
    this.orEmpty().split("\n").map { it.trim() }.filter { it.isNotEmpty() }

private fun CaptureEntity.statusLabel(): String =
    when (status) {
        CaptureStatus.Synced -> "Synced"
        CaptureStatus.LocalProblem -> "Local file missing"
        else -> "Pending sync"
    }

@Preview(showBackground = true)
@Composable
fun CaptureListItemPreview() {
    PkmsAppTheme {
        CaptureListItem(
            capture = CaptureEntity(
                "preview",
                "Capture meeting note and send it to the Obsidian Inbox.",
                "2026-07-19T12:00:00.000Z",
                "android",
                CaptureStatus.Pending,
                null,
                "https://example.com",
                "Work\nTODO",
                "/tmp/image.jpg",
                "image.jpg",
                "image/jpeg",
            ),
            onDelete = {},
        )
    }
}
