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

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Pocket dao
 */
public interface Pocket<T> {

    /**
     * Destroys the pocket
     */
    @CheckResult @NonNull
    Completable destroy();

    /**
     * Persists the value
     */
    @CheckResult @NonNull
    Completable write(@NonNull String key, @NonNull T value);

    /**
     * Reads the value for the key
     */
    @CheckResult @NonNull
    Maybe<T> read(@NonNull String key);

    /**
     * Reads the value for the key
     * if no value found it will return the default value
     */
    @CheckResult @NonNull
    Single<T> read(@NonNull String key, @NonNull T defaultValue);

    /**
     * Returns whether the key exists
     */
    @CheckResult @NonNull
    Single<Boolean> exists(@NonNull String key);

    /**
     * Returns the last modification time
     */
    @CheckResult @NonNull
    Single<Long> lastModified(@NonNull String key);

    /**
     * Deletes the value for the key
     */
    @CheckResult @NonNull
    Completable delete(@NonNull String key);

    /**
     * Returns all keys
     */
    @CheckResult @NonNull
    Single<List<String>> getAllKeys();

    /**
     * Returns all values
     */
    @CheckResult @NonNull
    Single<HashMap<String, T>> getAllValues();

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
    Flowable<T> latest(@NonNull final String key);

    /**
     * Emits on key changes
     * You have to cast the objects
     */
    @CheckResult @NonNull
    Flowable<Pair<String, T>> updates();

    final class Builder<T> {

        private final String DEFAULT_NAME = "pocket";

        private final Context context;
        private final Class<T> clazz;

        private String name;
        private Gson gson;
        private Scheduler scheduler;

        /**
         * Returns a new builder
         */
        public Builder(@NonNull Context context, @NonNull Class<T> clazz) {
            this.context = context;
            this.clazz = clazz;
        }

        /**
         * The name of the pocket
         */
        @NonNull
        public Builder<T> name(@NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Gson instance which will be used to serialize and deserialize values
         */
        @NonNull
        public Builder<T> gson(@NonNull Gson gson) {
            this.gson = gson;
            return this;
        }

        /**
         * The default scheduler
         */
        @NonNull
        public Builder<T> scheduler(@NonNull Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * Returns a new pocket instance
         */
        @NonNull
        public Pocket<T> build() {
            if (name == null) {
                name = DEFAULT_NAME;
            }
            if (gson == null) {
                gson = new Gson();
            }
            if (scheduler == null) {
                scheduler = Schedulers.io();
            }

            return new RealPocket<>(context, name, scheduler, gson, clazz);
        }
    }
}
