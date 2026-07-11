package com.borrownest.app.ui.navigation

/** All navigation routes. Item-scoped routes carry an itemId argument. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val BOARD = "board"
    const val HISTORY = "history"
    const val ARCHIVE = "archive"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
    const val RETURNED = "returned"
    const val PERSONS = "persons"

    const val ADD = "add"
    const val EDIT = "edit/{itemId}"
    const val DETAIL = "detail/{itemId}"
    const val PERSON_DETAIL = "person/{personName}"

    fun edit(itemId: String) = "edit/$itemId"
    fun detail(itemId: String) = "detail/$itemId"
}
