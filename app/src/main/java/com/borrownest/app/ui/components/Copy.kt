package com.borrownest.app.ui.components

/** Central place for reused legal/privacy copy so it stays consistent. */
object Copy {
    const val MANUAL_TRACKING_DISCLAIMER =
        "BorrowNest is a manual item-tracking tool. Item details, names, dates, " +
            "statuses, and notes are entered by the user. The app does not access " +
            "contacts, verify ownership, create legal agreements, contact other " +
            "people, or guarantee item return."

    const val PRIVACY_NOTE =
        "Names are entered manually and stored only on this device. BorrowNest does " +
            "not access your contacts or upload personal data."

    const val PRIVACY_NOTE_LONG =
        "BorrowNest stores item names, manually entered person names, dates, statuses, " +
            "notes, history, archive records, and settings locally on this device. The " +
            "app has no account, no cloud sync, no internet access, no ads, no analytics, " +
            "no payments, no contact access, no messaging, and no background monitoring."

    const val REMINDER_EXPLANATION =
        "BorrowNest reminders appear inside the app. The app does not send push " +
            "notifications or contact other people."

    const val PRIORITY_EXPLANATION =
        "High priority is a personal sorting preference."

    const val SAVED_MANUALLY_NOTE = "This status is saved manually."
}
