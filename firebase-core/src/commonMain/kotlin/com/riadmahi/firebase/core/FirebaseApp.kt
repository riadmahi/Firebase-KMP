package com.riadmahi.firebase.core

/**
 * The entry point of Firebase SDK.
 * A FirebaseApp represents a configured instance of Firebase services.
 */
expect class FirebaseApp {
    /**
     * The name of this FirebaseApp instance.
     */
    val name: String

    /**
     * The options used to configure this FirebaseApp.
     */
    val options: FirebaseOptions

    /**
     * Deletes this FirebaseApp instance.
     */
    fun delete()

    companion object {
        /**
         * Default app name.
         */
        val DEFAULT_APP_NAME: String

        /**
         * Initializes the default FirebaseApp using options from configuration files.
         */
        fun initialize(): FirebaseApp

        /**
         * Initializes a FirebaseApp with the given options.
         */
        fun initialize(options: FirebaseOptions): FirebaseApp

        /**
         * Initializes a named FirebaseApp with the given options.
         */
        fun initialize(options: FirebaseOptions, name: String): FirebaseApp

        /**
         * Returns the default FirebaseApp instance.
         * @throws FirebaseException.NotInitializedException if not initialized.
         */
        fun getInstance(): FirebaseApp

        /**
         * Returns the FirebaseApp instance with the given name.
         * @throws FirebaseException.NotInitializedException if not found.
         */
        fun getInstance(name: String): FirebaseApp

        /**
         * Returns all initialized FirebaseApp instances.
         */
        val apps: List<FirebaseApp>
    }
}
