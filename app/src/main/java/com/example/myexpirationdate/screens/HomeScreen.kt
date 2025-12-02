package com.example.myexpirationdate.screens

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.myexpirationdate.R
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.models.parseExpirationItem
import com.example.myexpirationdate.viewmodels.CameraVM
import com.example.myexpirationdate.viewmodels.OpenAiVM
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.text.get


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    cameraVM: CameraVM,
    openAiVM: OpenAiVM = viewModel()
) {
    val analysisResult by openAiVM.analysisResult.collectAsState()
    val isLoading by openAiVM.isLoading.collectAsState()
    val lastBitmap by openAiVM.lastBitmap.collectAsState()
    val saved = remember { mutableStateOf(false) }
    val dialogDismissed = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    var showManualEntryDialog by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Take a Photo to find its Expiration Date")

            Spacer(modifier = Modifier.height(20.dp))

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    openAiVM.setBitmap(bitmap)
                    saved.value = false
                    openAiVM.analyzeImage(bitmap)
                } else {
                    Log.e(TAG, "No bitmap returned from camera intent")
                }
            }


            val takePictureIntent = remember { Intent(MediaStore.ACTION_IMAGE_CAPTURE) }
            Button(
                onClick = {
                    launcher.launch(takePictureIntent)
                }
            ) {
                Text("Open Camera")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition()
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing)
                    )
                )

                AsyncImage(
                    model = R.drawable.refresh_24dp_1f1f1f_fill0_wght400_grad0_opsz24,
                    contentDescription = "loading symbol",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer {
                            rotationZ = angle
                        },
                    contentScale = ContentScale.Crop
                )
            }

            if (analysisResult.isNotEmpty() && !showManualEntryDialog) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Product Found",
                        style = typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Log.d(TAG, "Analysis Result: $analysisResult")
                    val parsed = parseExpirationItem(analysisResult)

                    if (parsed != null) {

                        if (lastBitmap != null && !saved.value) {
                            cameraVM.onTakePhoto(lastBitmap!!, parsed)
                            saved.value = true
                        }
                        Log.d(TAG, "Raw Analysis Result: $analysisResult")
                        Log.d(TAG, "Parsed isDonatable: ${parsed?.isDonatable}")


                        AnimatedVisibility(visible = true, enter = fadeIn(tween(500))) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    IconButton(
                                        onClick = {
                                            saved.value = false
                                            openAiVM.clearAnalysisResult()
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss"
                                        )
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(top = 0.dp)
                                    ) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            lastBitmap?.let { bmp ->
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(90.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column {
                                                Text(parsed.name, style = typography.titleLarge)
                                                Text(
                                                    "Expires in: ${parsed.months}m ${parsed.days}d",
                                                    style = typography.bodyMedium
                                                )
                                            }
                                        }

                                        Divider()

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Donatable", style = typography.bodyLarge)
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(if (parsed.isDonatable) "Yes" else "No") },
                                                leadingIcon = {
                                                    Icon(
                                                        if (parsed.isDonatable) Icons.Default.Check
                                                        else Icons.Default.Close,
                                                        contentDescription = null
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        // No match found
                        if (!saved.value && !dialogDismissed.value) {
                            showManualEntryDialog = true
                        }
                    }
                }
            }


            // Show manual entry dialog when no match is found
            if (showManualEntryDialog) {
                AddItemDialog(
                    onDismissRequest = {
                        showManualEntryDialog = false
                        dialogDismissed.value = true
                        openAiVM.clearAnalysisResult()
                    },
                    onSave = { name, months, days, isDonatable ->
                        cameraVM.onSaveManual(name, months, days, isDonatable, lastBitmap)
                        saved.value = true
                        dialogDismissed.value = true
                        showManualEntryDialog = false
                        openAiVM.clearAnalysisResult()

                        snackScope.launch {
                            snackbarHostState.showSnackbar("Entry saved")
                        }
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddItemDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, months: Int, days: Int, isDonatable: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp)
        ) {
            var nameState by remember { mutableStateOf("") }
            var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
            var isDonatable by remember { mutableStateOf(false) }
            var isDatePickerOpen by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No match found. Add item.",
                    style = typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    shape = shapes.large,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isDatePickerOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedDateMillis != null) "Change Date" else "Choose Expiration Date")
                }

                selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    Text(
                        text = "Selected: $date",
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Donatable:", style = typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isDonatable, onCheckedChange = { isDonatable = it })
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (nameState.isNotBlank() && selectedDateMillis != null) {
                                val today = LocalDate.now()
                                val selected = Instant.ofEpochMilli(selectedDateMillis!!)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                val period = Period.between(today, selected)
                                val months = period.years * 12 + period.months
                                val days = period.days

                                onSave(nameState, months, days, isDonatable)
                            }
                        },
                        enabled = nameState.isNotBlank() && selectedDateMillis != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }

            if (isDatePickerOpen) {
                DatePickerModalInput(
                    onDateSelected = { millis ->
                        selectedDateMillis = millis
                    },
                    onDismiss = { isDatePickerOpen = false }
                )
            }
        }
    }
}


@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}