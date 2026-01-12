package com.riadmahi.firebase.analytics

import com.riadmahi.firebase.core.FirebaseApp

/**
 * Firebase Analytics for Kotlin Multiplatform.
 *
 * Firebase Analytics collects usage and behavior data for your app.
 *
 * Example usage:
 * ```kotlin
 * val analytics = FirebaseAnalytics.getInstance()
 *
 * // Log a custom event
 * analytics.logEvent("level_complete") {
 *     param("level_name", "World 1-1")
 *     param("score", 9000)
 * }
 *
 * // Log a predefined event
 * analytics.logEvent(AnalyticsEvent.SELECT_CONTENT) {
 *     param(AnalyticsParam.ITEM_ID, "item_123")
 *     param(AnalyticsParam.CONTENT_TYPE, "product")
 * }
 *
 * // Set user properties
 * analytics.setUserProperty("favorite_food", "pizza")
 *
 * // Set user ID
 * analytics.setUserId("user_12345")
 * ```
 */
expect class FirebaseAnalytics {
    /**
     * Logs an event with the given name and parameters.
     *
     * @param name The event name.
     * @param params The event parameters.
     */
    fun logEvent(name: String, params: Map<String, Any>? = null)

    /**
     * Sets a user property to the given value.
     *
     * @param name The property name.
     * @param value The property value.
     */
    fun setUserProperty(name: String, value: String?)

    /**
     * Sets the user ID for this app instance.
     *
     * @param userId The user ID, or null to clear it.
     */
    fun setUserId(userId: String?)

    /**
     * Sets whether analytics collection is enabled for this app.
     *
     * @param enabled Whether to enable analytics collection.
     */
    fun setAnalyticsCollectionEnabled(enabled: Boolean)

    /**
     * Clears all analytics data for this app from the device.
     */
    fun resetAnalyticsData()

    /**
     * Sets the current screen name for analytics.
     *
     * @param screenName The screen name.
     * @param screenClass The screen class name (optional).
     */
    fun setCurrentScreen(screenName: String, screenClass: String? = null)

    /**
     * Sets the default event parameters that will be included with every event.
     *
     * @param params The default parameters, or null to clear them.
     */
    fun setDefaultEventParameters(params: Map<String, Any>?)

    /**
     * Gets the app instance ID.
     *
     * @param callback Callback with the instance ID.
     */
    fun getAppInstanceId(callback: (String?) -> Unit)

    /**
     * Sets the session timeout duration in milliseconds.
     *
     * @param milliseconds The timeout duration.
     */
    fun setSessionTimeoutDuration(milliseconds: Long)

    companion object {
        /**
         * Returns the [FirebaseAnalytics] instance for the default [FirebaseApp].
         */
        fun getInstance(): FirebaseAnalytics
    }
}

/**
 * Builder for analytics event parameters.
 */
class AnalyticsEventBuilder {
    private val params = mutableMapOf<String, Any>()

    fun param(key: String, value: String): AnalyticsEventBuilder {
        params[key] = value
        return this
    }

    fun param(key: String, value: Long): AnalyticsEventBuilder {
        params[key] = value
        return this
    }

    fun param(key: String, value: Double): AnalyticsEventBuilder {
        params[key] = value
        return this
    }

    fun param(key: String, value: Int): AnalyticsEventBuilder {
        params[key] = value.toLong()
        return this
    }

    fun build(): Map<String, Any> = params.toMap()
}

/**
 * Extension function for logging events with a builder.
 */
inline fun FirebaseAnalytics.logEvent(name: String, builder: AnalyticsEventBuilder.() -> Unit) {
    val params = AnalyticsEventBuilder().apply(builder).build()
    logEvent(name, params)
}

/**
 * Predefined Analytics events.
 */
object AnalyticsEvent {
    const val ADD_PAYMENT_INFO = "add_payment_info"
    const val ADD_SHIPPING_INFO = "add_shipping_info"
    const val ADD_TO_CART = "add_to_cart"
    const val ADD_TO_WISHLIST = "add_to_wishlist"
    const val BEGIN_CHECKOUT = "begin_checkout"
    const val EARN_VIRTUAL_CURRENCY = "earn_virtual_currency"
    const val GENERATE_LEAD = "generate_lead"
    const val JOIN_GROUP = "join_group"
    const val LEVEL_END = "level_end"
    const val LEVEL_START = "level_start"
    const val LEVEL_UP = "level_up"
    const val LOGIN = "login"
    const val POST_SCORE = "post_score"
    const val PURCHASE = "purchase"
    const val REFUND = "refund"
    const val REMOVE_FROM_CART = "remove_from_cart"
    const val SCREEN_VIEW = "screen_view"
    const val SEARCH = "search"
    const val SELECT_CONTENT = "select_content"
    const val SELECT_ITEM = "select_item"
    const val SELECT_PROMOTION = "select_promotion"
    const val SHARE = "share"
    const val SIGN_UP = "sign_up"
    const val SPEND_VIRTUAL_CURRENCY = "spend_virtual_currency"
    const val TUTORIAL_BEGIN = "tutorial_begin"
    const val TUTORIAL_COMPLETE = "tutorial_complete"
    const val UNLOCK_ACHIEVEMENT = "unlock_achievement"
    const val VIEW_CART = "view_cart"
    const val VIEW_ITEM = "view_item"
    const val VIEW_ITEM_LIST = "view_item_list"
    const val VIEW_PROMOTION = "view_promotion"
    const val VIEW_SEARCH_RESULTS = "view_search_results"
}

/**
 * Predefined Analytics parameters.
 */
object AnalyticsParam {
    const val ACHIEVEMENT_ID = "achievement_id"
    const val AFFILIATION = "affiliation"
    const val CAMPAIGN = "campaign"
    const val CHARACTER = "character"
    const val CONTENT = "content"
    const val CONTENT_TYPE = "content_type"
    const val COUPON = "coupon"
    const val CURRENCY = "currency"
    const val DISCOUNT = "discount"
    const val ITEM_BRAND = "item_brand"
    const val ITEM_CATEGORY = "item_category"
    const val ITEM_ID = "item_id"
    const val ITEM_LIST_ID = "item_list_id"
    const val ITEM_LIST_NAME = "item_list_name"
    const val ITEM_NAME = "item_name"
    const val ITEMS = "items"
    const val LEVEL = "level"
    const val LEVEL_NAME = "level_name"
    const val LOCATION_ID = "location_id"
    const val MEDIUM = "medium"
    const val METHOD = "method"
    const val PAYMENT_TYPE = "payment_type"
    const val PRICE = "price"
    const val QUANTITY = "quantity"
    const val SCORE = "score"
    const val SCREEN_CLASS = "screen_class"
    const val SCREEN_NAME = "screen_name"
    const val SEARCH_TERM = "search_term"
    const val SHIPPING = "shipping"
    const val SHIPPING_TIER = "shipping_tier"
    const val SOURCE = "source"
    const val TAX = "tax"
    const val TERM = "term"
    const val TRANSACTION_ID = "transaction_id"
    const val VALUE = "value"
    const val VIRTUAL_CURRENCY_NAME = "virtual_currency_name"
}
