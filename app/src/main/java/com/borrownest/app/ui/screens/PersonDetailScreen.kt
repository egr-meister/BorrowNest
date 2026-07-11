package com.borrownest.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.ui.components.EmptyState
import com.borrownest.app.ui.components.ItemTile
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.PersonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    viewModel: BorrowViewModel,
    personName: String,
    onBack: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val normalized = PersonUtils.normalize(personName)
    val records = uiState.data.items
        .filter { PersonUtils.normalize(it.personName) == normalized }
        .sortedByDescending { it.updatedAt }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(personName.ifBlank { "Person" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Records linked to this manually entered label.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (records.isEmpty()) {
                EmptyState(message = "No records for this person.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(items = records, key = { it.id }) { item ->
                        ItemTile(
                            item = item,
                            soonThresholdDays = uiState.soonThresholdDays,
                            onClick = { onItemClick(item.id) },
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
