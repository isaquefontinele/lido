package io.github.isaquefontinele.lido

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.isaquefontinele.lido.library.LibraryRepository
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Lido") }) }) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                LibraryPlaceholder()
            }
        }
    }
}

@Composable
private fun LibraryPlaceholder(repository: LibraryRepository = koinInject()) {
    val items by repository.observeItems().collectAsStateWithLifecycle(initialValue = emptyList())
    Text(
        text = if (items.isEmpty()) "Your library is empty" else "${items.size} saved",
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Preview
@Composable
private fun EmptyLibraryPreview() {
    MaterialTheme { Text("Your library is empty") }
}
