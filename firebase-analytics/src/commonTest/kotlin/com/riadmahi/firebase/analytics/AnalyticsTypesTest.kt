package com.riadmahi.firebase.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Firebase Analytics types to validate API consistency.
 *
 * Reference: https://firebase.google.com/docs/analytics
 * Reference: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/analytics/FirebaseAnalytics.Event
 * Reference: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/analytics/FirebaseAnalytics.Param
 */
class AnalyticsTypesTest {

    // region AnalyticsEventBuilder

    @Test
    fun `AnalyticsEventBuilder should build string params`() {
        val params = AnalyticsEventBuilder()
            .param("item_name", "Premium Subscription")
            .build()

        assertEquals("Premium Subscription", params["item_name"])
    }

    @Test
    fun `AnalyticsEventBuilder should build long params`() {
        val params = AnalyticsEventBuilder()
            .param("quantity", 5L)
            .build()

        assertEquals(5L, params["quantity"])
    }

    @Test
    fun `AnalyticsEventBuilder should build double params`() {
        val params = AnalyticsEventBuilder()
            .param("price", 9.99)
            .build()

        assertEquals(9.99, params["price"])
    }

    @Test
    fun `AnalyticsEventBuilder should build int params as long`() {
        val params = AnalyticsEventBuilder()
            .param("level", 10)
            .build()

        assertEquals(10L, params["level"])
    }

    @Test
    fun `AnalyticsEventBuilder should support chaining`() {
        val params = AnalyticsEventBuilder()
            .param("item_id", "SKU_123")
            .param("item_name", "Product Name")
            .param("price", 29.99)
            .param("quantity", 2)
            .build()

        assertEquals(4, params.size)
        assertEquals("SKU_123", params["item_id"])
        assertEquals("Product Name", params["item_name"])
        assertEquals(29.99, params["price"])
        assertEquals(2L, params["quantity"])
    }

    // endregion

    // region Predefined Events - matching Firebase Analytics.Event

    @Test
    fun `AnalyticsEvent should have e-commerce events`() {
        assertEquals("add_payment_info", AnalyticsEvent.ADD_PAYMENT_INFO)
        assertEquals("add_shipping_info", AnalyticsEvent.ADD_SHIPPING_INFO)
        assertEquals("add_to_cart", AnalyticsEvent.ADD_TO_CART)
        assertEquals("add_to_wishlist", AnalyticsEvent.ADD_TO_WISHLIST)
        assertEquals("begin_checkout", AnalyticsEvent.BEGIN_CHECKOUT)
        assertEquals("purchase", AnalyticsEvent.PURCHASE)
        assertEquals("refund", AnalyticsEvent.REFUND)
        assertEquals("remove_from_cart", AnalyticsEvent.REMOVE_FROM_CART)
        assertEquals("view_cart", AnalyticsEvent.VIEW_CART)
        assertEquals("view_item", AnalyticsEvent.VIEW_ITEM)
        assertEquals("view_item_list", AnalyticsEvent.VIEW_ITEM_LIST)
        assertEquals("view_promotion", AnalyticsEvent.VIEW_PROMOTION)
        assertEquals("select_promotion", AnalyticsEvent.SELECT_PROMOTION)
        assertEquals("select_item", AnalyticsEvent.SELECT_ITEM)
    }

    @Test
    fun `AnalyticsEvent should have gaming events`() {
        assertEquals("earn_virtual_currency", AnalyticsEvent.EARN_VIRTUAL_CURRENCY)
        assertEquals("spend_virtual_currency", AnalyticsEvent.SPEND_VIRTUAL_CURRENCY)
        assertEquals("level_start", AnalyticsEvent.LEVEL_START)
        assertEquals("level_end", AnalyticsEvent.LEVEL_END)
        assertEquals("level_up", AnalyticsEvent.LEVEL_UP)
        assertEquals("post_score", AnalyticsEvent.POST_SCORE)
        assertEquals("unlock_achievement", AnalyticsEvent.UNLOCK_ACHIEVEMENT)
    }

    @Test
    fun `AnalyticsEvent should have user engagement events`() {
        assertEquals("login", AnalyticsEvent.LOGIN)
        assertEquals("sign_up", AnalyticsEvent.SIGN_UP)
        assertEquals("tutorial_begin", AnalyticsEvent.TUTORIAL_BEGIN)
        assertEquals("tutorial_complete", AnalyticsEvent.TUTORIAL_COMPLETE)
        assertEquals("share", AnalyticsEvent.SHARE)
        assertEquals("join_group", AnalyticsEvent.JOIN_GROUP)
        assertEquals("generate_lead", AnalyticsEvent.GENERATE_LEAD)
    }

    @Test
    fun `AnalyticsEvent should have content events`() {
        assertEquals("screen_view", AnalyticsEvent.SCREEN_VIEW)
        assertEquals("search", AnalyticsEvent.SEARCH)
        assertEquals("select_content", AnalyticsEvent.SELECT_CONTENT)
        assertEquals("view_search_results", AnalyticsEvent.VIEW_SEARCH_RESULTS)
    }

    // endregion

    // region Predefined Parameters - matching Firebase Analytics.Param

    @Test
    fun `AnalyticsParam should have item parameters`() {
        assertEquals("item_id", AnalyticsParam.ITEM_ID)
        assertEquals("item_name", AnalyticsParam.ITEM_NAME)
        assertEquals("item_brand", AnalyticsParam.ITEM_BRAND)
        assertEquals("item_category", AnalyticsParam.ITEM_CATEGORY)
        assertEquals("item_list_id", AnalyticsParam.ITEM_LIST_ID)
        assertEquals("item_list_name", AnalyticsParam.ITEM_LIST_NAME)
        assertEquals("items", AnalyticsParam.ITEMS)
    }

    @Test
    fun `AnalyticsParam should have transaction parameters`() {
        assertEquals("currency", AnalyticsParam.CURRENCY)
        assertEquals("value", AnalyticsParam.VALUE)
        assertEquals("price", AnalyticsParam.PRICE)
        assertEquals("quantity", AnalyticsParam.QUANTITY)
        assertEquals("tax", AnalyticsParam.TAX)
        assertEquals("shipping", AnalyticsParam.SHIPPING)
        assertEquals("shipping_tier", AnalyticsParam.SHIPPING_TIER)
        assertEquals("transaction_id", AnalyticsParam.TRANSACTION_ID)
        assertEquals("coupon", AnalyticsParam.COUPON)
        assertEquals("discount", AnalyticsParam.DISCOUNT)
        assertEquals("payment_type", AnalyticsParam.PAYMENT_TYPE)
        assertEquals("affiliation", AnalyticsParam.AFFILIATION)
    }

    @Test
    fun `AnalyticsParam should have gaming parameters`() {
        assertEquals("level", AnalyticsParam.LEVEL)
        assertEquals("level_name", AnalyticsParam.LEVEL_NAME)
        assertEquals("score", AnalyticsParam.SCORE)
        assertEquals("character", AnalyticsParam.CHARACTER)
        assertEquals("achievement_id", AnalyticsParam.ACHIEVEMENT_ID)
        assertEquals("virtual_currency_name", AnalyticsParam.VIRTUAL_CURRENCY_NAME)
    }

    @Test
    fun `AnalyticsParam should have content parameters`() {
        assertEquals("content", AnalyticsParam.CONTENT)
        assertEquals("content_type", AnalyticsParam.CONTENT_TYPE)
        assertEquals("screen_name", AnalyticsParam.SCREEN_NAME)
        assertEquals("screen_class", AnalyticsParam.SCREEN_CLASS)
        assertEquals("search_term", AnalyticsParam.SEARCH_TERM)
        assertEquals("method", AnalyticsParam.METHOD)
    }

    @Test
    fun `AnalyticsParam should have campaign parameters`() {
        assertEquals("campaign", AnalyticsParam.CAMPAIGN)
        assertEquals("source", AnalyticsParam.SOURCE)
        assertEquals("medium", AnalyticsParam.MEDIUM)
        assertEquals("term", AnalyticsParam.TERM)
        assertEquals("location_id", AnalyticsParam.LOCATION_ID)
    }

    // endregion
}
