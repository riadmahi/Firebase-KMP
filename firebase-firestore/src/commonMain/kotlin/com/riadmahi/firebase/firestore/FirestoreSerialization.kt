package com.riadmahi.firebase.firestore

import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/**
 * Firestore serialization utilities for @Serializable data classes.
 *
 * Example usage:
 * ```kotlin
 * @Serializable
 * data class User(
 *     val name: String,
 *     val age: Int,
 *     val email: String? = null
 * )
 *
 * // Write a document
 * val user = User("John", 30, "john@example.com")
 * firestore.collection("users").document("john").set(user)
 *
 * // Read a document
 * val result = firestore.collection("users").document("john").get()
 * if (result is FirebaseResult.Success) {
 *     val user: User? = result.data.data()
 * }
 *
 * // Query documents
 * val users: List<User> = firestore.collection("users")
 *     .whereGreaterThan("age", 25)
 *     .get()
 *     .getOrNull()
 *     ?.documents
 *     ?.mapNotNull { it.data<User>() }
 *     ?: emptyList()
 * ```
 */

// ============================================================================
// DocumentReference Extensions
// ============================================================================

/**
 * Sets the document data from a @Serializable object.
 *
 * @param data The serializable object to write.
 * @param options The set options (default: overwrite).
 */
suspend inline fun <reified T> DocumentReference.set(
    data: T,
    options: SetOptions = SetOptions.Overwrite
): FirebaseResult<Unit> {
    val map = encodeToMap(data)
    return set(map, options)
}

/**
 * Updates the document with fields from a @Serializable object.
 * Only non-null fields will be updated.
 *
 * @param data The serializable object containing the fields to update.
 */
suspend inline fun <reified T> DocumentReference.update(data: T): FirebaseResult<Unit> {
    val map = encodeToMap(data)
    return update(map)
}

// ============================================================================
// DocumentSnapshot Extensions
// ============================================================================

/**
 * Decodes the document data to a @Serializable object.
 *
 * @return The decoded object, or null if the document doesn't exist or decoding fails.
 */
inline fun <reified T> DocumentSnapshot.data(): T? {
    val map = data() ?: return null
    return try {
        decodeFromMap(map)
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodes the document data to a @Serializable object.
 *
 * @return The decoded object.
 * @throws SerializationException if the document doesn't exist or decoding fails.
 */
inline fun <reified T> DocumentSnapshot.dataOrThrow(): T {
    val map = data() ?: throw SerializationException("Document does not exist")
    return decodeFromMap(map)
}

// ============================================================================
// CollectionReference Extensions
// ============================================================================

/**
 * Adds a new document with data from a @Serializable object.
 *
 * @param data The serializable object to write.
 * @return The reference to the newly created document.
 */
suspend inline fun <reified T> CollectionReference.add(data: T): FirebaseResult<DocumentReference> {
    val map = encodeToMap(data)
    return add(map)
}

// ============================================================================
// Transaction Extensions
// ============================================================================

/**
 * Gets and decodes a document to a @Serializable object within a transaction.
 *
 * @param documentRef The document reference.
 * @return The decoded object, or null if decoding fails.
 */
inline fun <reified T> Transaction.getData(documentRef: DocumentReference): T? {
    val snapshot = get(documentRef)
    return snapshot.data<T>()
}

/**
 * Sets document data from a @Serializable object within a transaction.
 *
 * @param documentRef The document reference.
 * @param data The serializable object to write.
 * @param options The set options (default: overwrite).
 */
inline fun <reified T> Transaction.set(
    documentRef: DocumentReference,
    data: T,
    options: SetOptions = SetOptions.Overwrite
): Transaction {
    val map = encodeToMap(data)
    return set(documentRef, map, options)
}

/**
 * Updates document data from a @Serializable object within a transaction.
 *
 * @param documentRef The document reference.
 * @param data The serializable object containing fields to update.
 */
inline fun <reified T> Transaction.update(
    documentRef: DocumentReference,
    data: T
): Transaction {
    val map = encodeToMap(data)
    return update(documentRef, map)
}

// ============================================================================
// WriteBatch Extensions
// ============================================================================

/**
 * Sets document data from a @Serializable object within a batch.
 *
 * @param documentRef The document reference.
 * @param data The serializable object to write.
 * @param options The set options (default: overwrite).
 */
inline fun <reified T> WriteBatch.set(
    documentRef: DocumentReference,
    data: T,
    options: SetOptions = SetOptions.Overwrite
): WriteBatch {
    val map = encodeToMap(data)
    return set(documentRef, map, options)
}

/**
 * Updates document data from a @Serializable object within a batch.
 *
 * @param documentRef The document reference.
 * @param data The serializable object containing fields to update.
 */
inline fun <reified T> WriteBatch.update(
    documentRef: DocumentReference,
    data: T
): WriteBatch {
    val map = encodeToMap(data)
    return update(documentRef, map)
}

// ============================================================================
// Encoding / Decoding
// ============================================================================

/**
 * Encodes a @Serializable object to a Map suitable for Firestore.
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> encodeToMap(value: T): Map<String, Any?> {
    val serializer = serializer<T>()
    val encoder = FirestoreEncoder()
    serializer.serialize(encoder, value)
    @Suppress("UNCHECKED_CAST")
    return encoder.result as? Map<String, Any?> ?: emptyMap()
}

/**
 * Decodes a Map from Firestore to a @Serializable object.
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> decodeFromMap(map: Map<String, Any?>): T {
    val serializer = serializer<T>()
    val decoder = FirestoreDecoder(map)
    return serializer.deserialize(decoder)
}

// ============================================================================
// Internal Encoder/Decoder Implementation
// ============================================================================

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal class FirestoreEncoder : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    var result: Any? = null
        private set

    private val stack = mutableListOf<Any?>()
    private val keyStack = mutableListOf<String?>()

    override fun encodeNull() {
        pushValue(null)
    }

    override fun encodeBoolean(value: Boolean) { pushValue(value) }
    override fun encodeByte(value: Byte) { pushValue(value.toLong()) }
    override fun encodeShort(value: Short) { pushValue(value.toLong()) }
    override fun encodeInt(value: Int) { pushValue(value.toLong()) }
    override fun encodeLong(value: Long) { pushValue(value) }
    override fun encodeFloat(value: Float) { pushValue(value.toDouble()) }
    override fun encodeDouble(value: Double) { pushValue(value) }
    override fun encodeChar(value: Char) { pushValue(value.toString()) }
    override fun encodeString(value: String) { pushValue(value) }
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        pushValue(enumDescriptor.getElementName(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val current: Any = when (descriptor.kind) {
            StructureKind.LIST -> mutableListOf<Any?>()
            StructureKind.MAP -> mutableMapOf<String, Any?>()
            else -> mutableMapOf<String, Any?>()
        }
        stack.add(current)
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        val completed = stack.removeLastOrNull()
        if (stack.isEmpty()) {
            result = completed
        } else {
            val parent = stack.last()
            val key = keyStack.removeLastOrNull()
            when (parent) {
                is MutableMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    (parent as MutableMap<String, Any?>)[key!!] = completed
                }
                is MutableList<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    (parent as MutableList<Any?>).add(completed)
                }
            }
        }
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (descriptor.kind == StructureKind.CLASS || descriptor.kind == StructureKind.OBJECT) {
            keyStack.add(descriptor.getElementName(index))
        }
        return true
    }

    private fun pushValue(value: Any?) {
        val parent = stack.lastOrNull()
        when (parent) {
            is MutableMap<*, *> -> {
                val key = keyStack.removeLastOrNull()
                @Suppress("UNCHECKED_CAST")
                (parent as MutableMap<String, Any?>)[key!!] = value
            }
            is MutableList<*> -> {
                @Suppress("UNCHECKED_CAST")
                (parent as MutableList<Any?>).add(value)
            }
            else -> result = value
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal class FirestoreDecoder(
    private val data: Any?,
    private val serializersModule_: SerializersModule = EmptySerializersModule()
) : AbstractDecoder() {

    override val serializersModule: SerializersModule = serializersModule_

    private var currentIndex = 0
    private var currentKey: String? = null
    private val mapData: Map<String, Any?>? get() = data as? Map<String, Any?>
    private val listData: List<Any?>? get() = data as? List<Any?>

    override fun decodeBoolean(): Boolean = (data as? Boolean) ?: false
    override fun decodeByte(): Byte = (data as? Number)?.toByte() ?: 0
    override fun decodeShort(): Short = (data as? Number)?.toShort() ?: 0
    override fun decodeInt(): Int = (data as? Number)?.toInt() ?: 0
    override fun decodeLong(): Long = (data as? Number)?.toLong() ?: 0L
    override fun decodeFloat(): Float = (data as? Number)?.toFloat() ?: 0f
    override fun decodeDouble(): Double = (data as? Number)?.toDouble() ?: 0.0
    override fun decodeChar(): Char = (data as? String)?.firstOrNull() ?: ' '
    override fun decodeString(): String = data?.toString() ?: ""

    override fun decodeNull(): Nothing? = null
    override fun decodeNotNullMark(): Boolean = data != null

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val name = data as? String ?: return 0
        return (0 until enumDescriptor.elementsCount).firstOrNull {
            enumDescriptor.getElementName(it) == name
        } ?: 0
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        when (descriptor.kind) {
            StructureKind.LIST -> {
                val list = listData ?: return CompositeDecoder.DECODE_DONE
                return if (currentIndex < list.size) currentIndex else CompositeDecoder.DECODE_DONE
            }
            StructureKind.MAP -> {
                val map = mapData ?: return CompositeDecoder.DECODE_DONE
                val keys = map.keys.toList()
                return if (currentIndex < keys.size * 2) currentIndex else CompositeDecoder.DECODE_DONE
            }
            else -> {
                val map = mapData ?: return CompositeDecoder.DECODE_DONE
                while (currentIndex < descriptor.elementsCount) {
                    val name = descriptor.getElementName(currentIndex)
                    if (map.containsKey(name)) {
                        currentKey = name
                        return currentIndex++
                    }
                    currentIndex++
                }
                return CompositeDecoder.DECODE_DONE
            }
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return FirestoreDecoder(data, serializersModule_)
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val value = when (descriptor.kind) {
            StructureKind.LIST -> {
                listData?.getOrNull(index)
            }
            StructureKind.MAP -> {
                val map = mapData ?: emptyMap()
                val keys = map.keys.toList()
                if (index % 2 == 0) {
                    keys.getOrNull(index / 2)
                } else {
                    map[keys.getOrNull(index / 2)]
                }
            }
            else -> {
                val key = descriptor.getElementName(index)
                mapData?.get(key)
            }
        }
        currentIndex = index + 1
        return deserializer.deserialize(FirestoreDecoder(value, serializersModule_))
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return when (descriptor.kind) {
            StructureKind.LIST -> listData?.size ?: 0
            StructureKind.MAP -> mapData?.size ?: 0
            else -> -1
        }
    }
}
