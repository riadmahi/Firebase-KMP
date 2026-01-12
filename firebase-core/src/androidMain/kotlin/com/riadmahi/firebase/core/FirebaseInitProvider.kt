package com.riadmahi.firebase.core

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * ContentProvider that automatically initializes the Firebase context at app startup.
 * This runs before Application.onCreate() and ensures FirebaseContext is available.
 *
 * This approach is used by the official Firebase SDK to get the application context
 * without requiring explicit initialization.
 */
internal class FirebaseInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.let { ctx ->
            FirebaseContext.setContext(ctx.applicationContext)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
