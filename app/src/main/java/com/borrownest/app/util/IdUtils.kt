package com.borrownest.app.util

import java.util.UUID

/** Local, offline ID generation. */
object IdUtils {
    fun newId(): String = UUID.randomUUID().toString()
}
