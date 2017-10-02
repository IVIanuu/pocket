/*
 * Copyright 2017 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.pocket.impl;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivianuu.pocket.Cache;
import com.ivianuu.pocket.Encryption;
import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.Serializer;
import com.ivianuu.pocket.Storage;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static com.ivianuu.pocket.Preconditions.checkNotNull;

/**
 * Pocket implementation
 */
public class RealPocket implements Pocket {

    private final InternalPocket internalPocket;

    public RealPocket(@NonNull Storage storage,
                      @NonNull Serializer serializer) {
        this(null, null, storage, serializer, null);
    }

    public RealPocket(@Nullable Cache cache,
                      @Nullable Encryption encryption,
                      @NonNull Storage storage,
                      @NonNull Serializer serializer,
                      @Nullable Scheduler scheduler) {
        checkNotNull(storage, "storage == null");
        checkNotNull(serializer, "serializer == null");
        if (cache == null) {
            cache = NoOpCache.create();
        }
        if (encryption == null) {
            encryption = NoOpEncryption.create();
        }

        internalPocket = new RealInternalPocket(
                cache, encryption, storage, serializer, scheduler);
    }

    @CheckResult
    @NonNull
    @Override
    public Completable put(@NonNull final String key, @NonNull final Object value) {
        checkNotNull(key, "key == null");
        checkNotNull(value, "value == null");
        return internalPocket.put(key, value);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Maybe<T> get(@NonNull final String key, @NonNull final Class<T> clazz) {
        checkNotNull(key, "key == null");
        checkNotNull(clazz, "clazz == null");
        return internalPocket.get(key, clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Maybe<T> get(@NonNull final String key, @NonNull final Type type) {
        checkNotNull(key, "key == null");
        checkNotNull(type, "type == null");
        return internalPocket.get(key, type);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<T> get(@NonNull String key, @NonNull T defaultValue, @NonNull Class<T> clazz) {
        checkNotNull(key, "key == null");
        checkNotNull(defaultValue, "defaultValue == null");
        checkNotNull(clazz, "clazz == null");
        return internalPocket.get(key, defaultValue, clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<T> get(@NonNull String key, @NonNull T defaultValue, @NonNull Type type) {
        checkNotNull(key, "key == null");
        checkNotNull(defaultValue, "defaultValue == null");
        checkNotNull(type, "type == null");
        return internalPocket.get(key, defaultValue, type);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<Option<T>> getOptional(@NonNull String key, @NonNull Class<T> clazz) {
        checkNotNull(key, "key == null");
        checkNotNull(clazz, "clazz == null");
        return internalPocket.getOptional(key, clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<Option<T>> getOptional(@NonNull String key, @NonNull Type type) {
        checkNotNull(key, "key == null");
        checkNotNull(type, "type == null");
        return internalPocket.getOptional(key, type);
    }

    @CheckResult
    @NonNull
    @Override
    public Completable delete(@NonNull final String key) {
        checkNotNull(key, "key == null");
        return internalPocket.delete(key);
    }

    @CheckResult
    @NonNull
    @Override
    public Completable deleteAll() {
        return internalPocket.deleteAll();
    }

    @CheckResult
    @NonNull
    @Override
    public Single<Boolean> contains(@NonNull final String key) {
        checkNotNull(key, "key == null");
        return internalPocket.contains(key);
    }

    @CheckResult
    @NonNull
    @Override
    public Single<List<String>> getAllKeys() {
        return internalPocket.getAllKeys();
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<Map<String, T>> getAll(@NonNull Class<T> clazz) {
        checkNotNull(clazz, "clazz == null");
        return internalPocket.getAll(clazz);
    }

    @CheckResult @NonNull
    @Override
    public <T> Single<Map<String, T>> getAll(@NonNull final Type type) {
        checkNotNull(type, "type == null");
        return internalPocket.getAll(type);
    }

    @CheckResult
    @NonNull
    @Override
    public Single<Integer> getCount() {
        return internalPocket.getCount();
    }

    @CheckResult
    @NonNull
    @Override
    public Flowable<String> keyChanges() {
        return internalPocket.keyChanges();
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Map.Entry<String, T>> stream(@NonNull Class<T> clazz) {
        checkNotNull(clazz, "clazz == null");
        return internalPocket.stream(clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Map.Entry<String, T>> stream(@NonNull final Type type) {
        checkNotNull(type, "type == null");
        return internalPocket.stream(type);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Option<T>> stream(@NonNull final String key, @NonNull final Class<T> clazz) {
        checkNotNull(key, "key == null");
        checkNotNull(clazz, "clazz == null");
        return internalPocket.stream(key, clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Option<T>> stream(@NonNull final String key, @NonNull final Type type) {
        checkNotNull(key, "key == null");
        checkNotNull(type, "type == null");
        return internalPocket.stream(key, type);
    }

    /**
     * Returns the cached value for this key
     */
    @CheckResult @NonNull
    protected <T> Maybe<T> cached(@NonNull String key) {
        checkNotNull(key, "key == null");
        return internalPocket.cached(key);
    }
}