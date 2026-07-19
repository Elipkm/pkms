package at.ee.pkmsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CaptureScreen(
    repository: CaptureRepository,
    syncScheduler: CaptureSyncScheduler,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val pendingCount by repository.pendingCount.observeAsState(0)
    val syncedCount by repository.syncedCount.observeAsState(0)
    val latestCaptures by repository.latestCaptures.observeAsState(emptyList())
    var noteText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
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
            minLines = 6,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                scope.launch {
                    val saved = repository.saveNote(noteText)
                    if (saved) {
                        noteText = ""
                        syncScheduler.enqueueSync()
                    }
                }
            },
            enabled = noteText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save note")
        }
        Text(
            text = "Recent captures",
            style = MaterialTheme.typography.titleMedium,
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(latestCaptures, key = { it.id }) { capture ->
                CaptureListItem(capture = capture)
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
private fun CaptureListItem(capture: CaptureEntity) {
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
                text = capture.content,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (capture.status == CaptureStatus.Synced) "Synced" else "Pending sync",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaptureListItemPreview() {
    PkmsAppTheme {
        CaptureListItem(
            CaptureEntity(
                "preview",
                "Capture meeting note and send it to the Obsidian Inbox.",
                "2026-07-19T12:00:00.000Z",
                "android",
                CaptureStatus.Pending,
                null,
            )
        )
    }
}
