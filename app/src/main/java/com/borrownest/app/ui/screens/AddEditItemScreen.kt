package com.borrownest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemCategory
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemPriority
import com.borrownest.app.ui.components.Copy
import com.borrownest.app.ui.components.ConfirmDialog
import com.borrownest.app.ui.components.DateField
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.DateUtils
import com.borrownest.app.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    viewModel: BorrowViewModel,
    itemId: String?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val existing = remember(itemId) { viewModel.itemById(itemId) }
    val isEdit = existing != null

    var direction by remember { mutableStateOf(existing?.direction ?: ItemDirection.Given) }
    var itemName by remember { mutableStateOf(existing?.itemName ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: ItemCategory.Book) }
    var customCategory by remember { mutableStateOf(existing?.customCategoryName ?: "") }
    var personName by remember { mutableStateOf(existing?.personName ?: "") }
    var recordDate by remember {
        mutableStateOf(existing?.recordDate ?: DateUtils.format(DateUtils.today()))
    }
    var expectedReturnDate by remember { mutableStateOf(existing?.expectedReturnDate ?: "") }
    var priorityHigh by remember { mutableStateOf(existing?.priority == ItemPriority.High) }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    var attemptedSave by remember { mutableStateOf(false) }
    var allowReturnBeforeRecord by remember { mutableStateOf(false) }
    var showConfirmEarlyReturn by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    fun buildDraft() = BorrowItem(
        id = existing?.id ?: "",
        direction = direction,
        itemName = itemName,
        category = category,
        customCategoryName = if (category == ItemCategory.Other) customCategory else "",
        personName = personName,
        recordDate = recordDate,
        expectedReturnDate = expectedReturnDate,
        actualReturnDate = existing?.actualReturnDate ?: "",
        lifecycleState = existing?.lifecycleState ?: com.borrownest.app.model.ItemLifecycleState.Active,
        priority = if (priorityHigh) ItemPriority.High else ItemPriority.Normal,
        note = note.take(ValidationUtils.NOTE_LIMIT),
        archived = existing?.archived ?: false,
        createdAt = existing?.createdAt ?: "",
        updatedAt = existing?.updatedAt ?: ""
    )

    val validation = ValidationUtils.validate(buildDraft(), allowReturnBeforeRecord)

    fun performSave() {
        val draft = buildDraft()
        if (isEdit) viewModel.updateItem(draft) else viewModel.addItem(draft)
        onDone()
    }

    fun onSaveClicked() {
        attemptedSave = true
        val current = ValidationUtils.validate(buildDraft(), allowReturnBeforeRecord)
        if (current.needsReturnBeforeRecordConfirm) {
            showConfirmEarlyReturn = true
            return
        }
        if (current.isValid) performSave()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit item" else "Add item") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Direction", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                SegmentedButton(
                    selected = direction == ItemDirection.Given,
                    onClick = { direction = ItemDirection.Given },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Given") }
                SegmentedButton(
                    selected = direction == ItemDirection.Borrowed,
                    onClick = { direction = ItemDirection.Borrowed },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Borrowed") }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item name *") },
                singleLine = true,
                isError = attemptedSave && validation.itemNameError != null,
                supportingText = {
                    if (attemptedSave && validation.itemNameError != null) {
                        Text(validation.itemNameError!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ItemCategory.entries.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.label) },
                            onClick = { category = c; categoryExpanded = false }
                        )
                    }
                }
            }

            if (category == ItemCategory.Other) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it.take(60) },
                    label = { Text("Custom category (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it.take(ValidationUtils.NAME_LIMIT) },
                label = {
                    Text(if (direction == ItemDirection.Given) "Given to *" else "Borrowed from *")
                },
                singleLine = true,
                isError = attemptedSave && validation.personNameError != null,
                supportingText = {
                    if (attemptedSave && validation.personNameError != null) {
                        Text(validation.personNameError!!)
                    } else {
                        Text("Entered manually. No contacts are accessed.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            DateField(
                label = "Record date *",
                value = recordDate,
                onValueChange = { recordDate = it },
                isError = attemptedSave && validation.recordDateError != null,
                supportingText = if (attemptedSave) validation.recordDateError else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            DateField(
                label = "Expected return date (optional)",
                value = expectedReturnDate,
                onValueChange = { expectedReturnDate = it; allowReturnBeforeRecord = false },
                optional = true,
                isError = attemptedSave && validation.expectedReturnDateError != null,
                supportingText = if (attemptedSave) validation.expectedReturnDateError else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("High priority", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        Copy.PRIORITY_EXPLANATION,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = priorityHigh, onCheckedChange = { priorityHigh = it })
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= ValidationUtils.NOTE_LIMIT) note = it },
                label = { Text("Note (optional)") },
                minLines = 3,
                supportingText = { Text("${ValidationUtils.NOTE_LIMIT - note.length} characters left") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text(
                Copy.MANUAL_TRACKING_DISCLAIMER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            Button(onClick = { onSaveClicked() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEdit) "Save changes" else "Save item")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showConfirmEarlyReturn) {
        ConfirmDialog(
            title = "Unusual return date",
            message = "The expected return date is before the record date. This is allowed " +
                "for historical or imported records. Keep it anyway?",
            confirmLabel = "Keep date",
            onConfirm = {
                allowReturnBeforeRecord = true
                showConfirmEarlyReturn = false
                // Re-validate with the allowance and save if valid.
                val current = ValidationUtils.validate(buildDraft(), true)
                if (current.isValid) performSave()
            },
            onDismiss = { showConfirmEarlyReturn = false }
        )
    }
}
