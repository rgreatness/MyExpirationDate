// kotlin
package com.example.myexpirationdate.screens

import android.view.ContextThemeWrapper
import android.widget.DatePicker as AndroidDatePicker
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myexpirationdate.TAG
import com.example.myexpirationdate.models.parseExpirationItem
import com.example.myexpirationdate.viewmodels.CameraVM
import com.example.myexpirationdate.viewmodels.OpenAiVM
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    cameraVM: CameraVM,
    openAiVM: OpenAiVM = viewModel(),
    modifier: Modifier = Modifier
) {
    val analysisResult by openAiVM.analysisResult.collectAsState()

    val lastBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val saved = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
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
                lastBitmap.value = bitmap
                saved.value = false
                openAiVM.analyzeImage(bitmap)
            } else {
                Log.e(TAG, "No bitmap returned from camera intent")
            }
        }

        val takePictureIntent = remember { Intent(MediaStore.ACTION_IMAGE_CAPTURE) }
        Button(onClick = { launcher.launch(takePictureIntent) }) {
            Text("Open Camera")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (analysisResult.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Product Analysis",
                    style = typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Log.d(TAG, "Analysis Result: $analysisResult")
                val parsed = parseExpirationItem(analysisResult)

                if (parsed != null) {
                    if (lastBitmap.value != null && !saved.value) {
                        cameraVM.onTakePhoto(lastBitmap.value!!, parsed)
                        saved.value = true
                    }

                    Text("Name: ${parsed.name}", style = typography.bodyLarge)
                    Text(
                        "Expiration: ${parsed.months} months, ${parsed.days} days",
                        style = typography.bodyLarge
                    )
                    Text("Donatable: ${parsed.isDonatable}", style = typography.bodyLarge)
                } else {
                    // Parsing yielded nothing known from DB -> allow manual entry (pre-fill name with analysis text)
                    ManualEntryForm(
                        cameraVM = cameraVM,
                        lastBitmap = lastBitmap,
                        saved = saved,
                        prefillName = analysisResult
                    )
                }
            }
        } else {
            // No analysis available -> allow manual entry with empty fields
            Text(
                "No analysis available. Please enter details manually.",
                style = typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            ManualEntryForm(
                cameraVM = cameraVM,
                lastBitmap = lastBitmap,
                saved = saved,
                prefillName = ""
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ManualEntryForm(
    cameraVM: CameraVM,
    lastBitmap: androidx.compose.runtime.MutableState<Bitmap?>,
    saved: androidx.compose.runtime.MutableState<Boolean>,
    prefillName: String
) {
    val context = LocalContext.current
    val nameState = remember { mutableStateOf(prefillName) }
    val expiryDateState = remember { mutableStateOf<LocalDate?>(null) }
    val donatableState = remember { mutableStateOf(false) }

    // state to control showing the modal composable (fixes the @Composable-in-onClick error)
    val showDatePickerModal = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(8.dp)
    ) {
        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            shape = shapes.large,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            label = { Text("Product Name") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        val pickedLabel = expiryDateState.value?.toString() ?: "No date selected"
        Text("Expiry: $pickedLabel", style = typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Embedded native DatePicker (spinner-themed) using AndroidView
        val now = LocalDate.now()
        val initYear = now.year
        val initMonthZeroBased = now.monthValue - 1
        val initDay = now.dayOfMonth

        AndroidView(
            factory = { ctx ->
                val themedCtx = ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth)
                AndroidDatePicker(themedCtx).apply {
                    init(initYear, initMonthZeroBased, initDay) { _, year, month, dayOfMonth ->
                        expiryDateState.value = LocalDate.of(year, month + 1, dayOfMonth)
                    }
                    try {
                        calendarViewShown = false
                        val dpClass = javaClass
                        val setSpinners = try {
                            dpClass.getMethod("setSpinnersShown", java.lang.Boolean.TYPE)
                        } catch (_: NoSuchMethodException) { null }
                        setSpinners?.invoke(this, true)
                    } catch (_: Exception) { /* ignore */ }
                }
            },
            update = { view ->
                expiryDateState.value?.let { d ->
                    if (view.year != d.year || view.month != d.monthValue - 1 || view.dayOfMonth != d.dayOfMonth) {
                        view.updateDate(d.year, d.monthValue - 1, d.dayOfMonth)
                    }
                }
            },
            modifier = Modifier.height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // button to open the Material date picker modal
        TextButton(onClick = { showDatePickerModal.value = true }) {
            Text("Open Date Input")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Donatable:", style = typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Switch(checked = donatableState.value, onCheckedChange = { donatableState.value = it })
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            val name = nameState.value.trim()
            val selected = expiryDateState.value
            if (name.isNotEmpty() && selected != null) {
                val today = LocalDate.now()
                val months: Int
                val days: Int
                if (!selected.isBefore(today)) {
                    val p: Period = Period.between(today, selected)
                    months = p.years * 12 + p.months
                    days = p.days
                } else {
                    months = 0
                    days = 0
                }

                cameraVM.onSaveManual(name, months, days, donatableState.value, lastBitmap.value)
                saved.value = true
            } else {
                Log.w(TAG, "Manual save prevented: name or date is empty")
            }
        }) {
            Text("Save")
        }

        if (saved.value) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Saved.", style = typography.bodySmall)
        }

        // Render modal composable conditionally (composable context)
        if (showDatePickerModal.value) {
            DatePickerModalInput(
                onDateSelected = { selectedMillis ->
                    if (selectedMillis != null) {
                        val localDate = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        expiryDateState.value = localDate
                    }
                },
                onDismiss = { showDatePickerModal.value = false }
            )
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
