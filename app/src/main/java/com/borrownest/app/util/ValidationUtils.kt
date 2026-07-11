package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemCategory

/** Result of validating an add/edit form. */
data class ValidationResult(
    val itemNameError: String? = null,
    val personNameError: String? = null,
    val recordDateError: String? = null,
    val expectedReturnDateError: String? = null,
    val customCategoryError: String? = null,
    val needsReturnBeforeRecordConfirm: Boolean = false
) {
    val isValid: Boolean
        get() = itemNameError == null && personNameError == null &&
            recordDateError == null && expectedReturnDateError == null &&
            customCategoryError == null
}

object ValidationUtils {

    const val NOTE_LIMIT = 1000
    const val NAME_LIMIT = 100

    fun validate(draft: BorrowItem, allowReturnBeforeRecord: Boolean): ValidationResult {
        val itemNameError = if (draft.itemName.isBlank()) "Item name is required." else null
        val personNameError = if (draft.personName.isBlank()) "Person name is required." else null

        val recordDateError = when {
            draft.recordDate.isBlank() -> "Record date is required."
            !DateUtils.isValid(draft.recordDate) -> "Record date is not valid."
            else -> null
        }

        var expectedError: String? = null
        var needsConfirm = false
        if (draft.expectedReturnDate.isNotBlank()) {
            if (!DateUtils.isValid(draft.expectedReturnDate)) {
                expectedError = "Expected return date is not valid."
            } else if (recordDateError == null) {
                val record = DateUtils.parse(draft.recordDate)
                val expected = DateUtils.parse(draft.expectedReturnDate)
                if (record != null && expected != null && expected.isBefore(record)) {
                    if (allowReturnBeforeRecord) {
                        needsConfirm = false
                    } else {
                        needsConfirm = true
                        expectedError = "Return date is before the record date."
                    }
                }
            }
        }

        val customError = if (draft.category == ItemCategory.Other &&
            draft.customCategoryName.isBlank()
        ) {
            null // Custom name is optional; "Other" alone is allowed.
        } else null

        return ValidationResult(
            itemNameError = itemNameError,
            personNameError = personNameError,
            recordDateError = recordDateError,
            expectedReturnDateError = expectedError,
            customCategoryError = customError,
            needsReturnBeforeRecordConfirm = needsConfirm
        )
    }
}
