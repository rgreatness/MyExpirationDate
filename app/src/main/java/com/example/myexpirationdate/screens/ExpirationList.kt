package com.example.myexpirationdate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myexpirationdate.models.ExpirationItem
import com.example.myexpirationdate.models.loadExpirationList

@Composable
fun ExpirationListScreen() {
    val context = LocalContext.current
    val expirationList = remember { loadExpirationList(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(expirationList) { item ->
            ExpirationItemCard(item)
        }
    }
}

@Composable
fun ExpirationItemCard(item: ExpirationItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.bodySmall
            )

            // Display all acceptable expiration dates
            item.acceptableXdate.forEachIndexed { index, xdate ->
                Text(
                    text = "Expires in: ${xdate.months} months, ${xdate.days} days",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Donatable: ${if (item.isDonateable) "Yes" else "No"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Unopened: ${if (item.isUnopened) "Yes" else "No"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
