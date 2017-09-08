package com.ivianuu.pocket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Storage
 */
public interface Storage<T> {

    /**
     * Inserts the value
     */
    void put(@NonNull String key, @NonNull T value);

    /**
     * Returns the value of the key or null
     */
    @Nullable
    T get(@NonNull String key);

    /**
     * Deletes the key
     */
    void delete(@NonNull String key);

    /**
     * Deletes all values
     */
    void deleteAll();

    /**
     * Returns whether the storage contains the key
     */
    boolean contains(@NonNull String key);

    /**
     * Returns last modification time
     */
    long lastModified(@NonNull String key);

    /**
     * Returns all keys in the path
     */
    @NonNull
    List<String> getAllKeys();

}
