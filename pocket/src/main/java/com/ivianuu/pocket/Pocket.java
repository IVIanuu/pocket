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

package com.ivianuu.pocket;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Pocket dao
 */
public interface Pocket {

    /**
     * Persists the value
     */
    @CheckResult @NonNull
    <T> Completable put(@NonNull String key, @NonNull T value);

    /**
     * Reads the value for the key
     */
    @CheckResult @NonNull
    <T> Maybe<T> get(@NonNull String key, @NonNull Class<T> clazz);

    /**
     * Reads the value for the key
     * if no value found it will return the default value
     */
    @CheckResult @NonNull
    <T> Single<T> get(@NonNull String key, @NonNull T defaultValue, @NonNull Class<T> clazz);

    /**
     * Deletes the value for the key
     */
    @CheckResult @NonNull
    Completable delete(@NonNull String key);

    /**
     * Deletes all values
     */
    @CheckResult @NonNull
    Completable deleteAll();

    /**
     * Returns whether the key exists
     */
    @CheckResult @NonNull
    Single<Boolean> contains(@NonNull String key);

    /**
     * Returns all keys
     */
    @CheckResult @NonNull
    Single<List<String>> getAllKeys();

    /**
     * Emits on key changes
     */
    @CheckResult @NonNull
    Flowable<String> keyChanges();

    /**
     * Emits on value changes
     * Emits on subscribe
     */
    @CheckResult @NonNull
    <T> Flowable<T> stream(@NonNull final String key, @NonNull Class<T> clazz);
}
