package com.ivianuu.pocket.sharedpreferencesstorage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivianuu.pocket.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared preferences storage implementation
 */
@SuppressLint("ApplySharedPref")
public final class SharedPreferencesStorage implements Storage {

    private final SharedPreferences sharedPreferences;

    private SharedPreferencesStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Returns a new shared preferences storage
     * This will use the default shared preferences
     */
    @NonNull
    public static Storage create(@NonNull Context context) {
        return create(PreferenceManager.getDefaultSharedPreferences(context));
    }

    /**
     * Returns a new shared preferences storage
     * This will use the shared preferences with the provided name
     */
    @NonNull
    public static Storage create(@NonNull Context context, @NonNull String name) {
        return create(context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    /**
     * Returns a new shared preferences storage
     */
    @NonNull
    public static Storage create(@NonNull SharedPreferences sharedPreferences) {
        return new SharedPreferencesStorage(sharedPreferences);
    }

    @Override
    public void put(@NonNull String key, @NonNull String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    @Nullable
    @Override
    public String get(@NonNull String key) {
        String value = sharedPreferences.getString(key, "");
        return !value.isEmpty() ? value : null;
    }

    @Override
    public void delete(@NonNull String key) {
        sharedPreferences.edit().remove(key).commit();
    }

    @Override
    public void deleteAll() {
        sharedPreferences.edit().clear().commit();
    }

    @Override
    public boolean contains(@NonNull String key) {
        return sharedPreferences.contains(key);
    }

    @NonNull
    @Override
    public List<String> getAllKeys() {
        return new ArrayList<>(sharedPreferences.getAll().keySet());
    }
}
