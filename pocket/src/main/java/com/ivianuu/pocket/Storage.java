package com.ivianuu.pocket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Storage
 */
interface Storage<T> {

    /**
     * Deletes all values
     */
    void destroy();

    /**
     * Inserts the value
     */
    void insert(@NonNull String key, @NonNull T value);

    /**
     * Returns the value of the key or null
     */
    @Nullable
    T select(@NonNull String key);

    /**
     * Returns whether the key exists
     */
    boolean exist(@NonNull String key);

    /**
     * Returns last modification time
     */
    long lastModified(@NonNull String key);

    /**
     * Deletes the key
     */
    void deleteIfExists(@NonNull String key);

    /**
     * Returns all keys in the path
     */
    @NonNull
    List<String> getAllKeys();

}
