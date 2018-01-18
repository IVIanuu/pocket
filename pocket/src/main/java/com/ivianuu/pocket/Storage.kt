package com.ivianuu.pocket

/**
 * Storage
 */
interface Storage {

    /**
     * Inserts the [value]
     */
    fun put(key: String, value: String)

    /**
     * Returns the value of the key or null
     */
    fun get(key: String): String?

    /**
     * Deletes the key
     */
    fun delete(key: String)

    /**
     * Deletes all values
     */
    fun deleteAll()

    /**
     * Returns whether the storage contains the key
     */
    fun contains(key: String): Boolean

    /**
     * Returns all keys in the path
     */
    fun getAllKeys(): List<String>
}
