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

import android.support.annotation.NonNull;

import com.google.gson.Gson;

/**
 * Gson serializer implementation
 */
public class GsonSerializer<T> implements Serializer<T> {

    private final Class<T> clazz;
    private final Gson gson;

    private GsonSerializer(Class<T> clazz, Gson gson) {
        this.clazz = clazz;
        this.gson = gson;
    }

    /**
     * Returns a gson serializer for the passed class
     * This will create a new gson instance
     */
    @NonNull
    public static <T> Serializer<T> create(@NonNull Class<T> clazz) {
        return create(clazz, new Gson());
    }

    /**
     * Returns a gson serializer for the passed class
     */
    @NonNull
    public static <T> Serializer<T> create(@NonNull Class<T> clazz, @NonNull Gson gson) {
        return new GsonSerializer<>(clazz, gson);
    }

    @NonNull
    @Override
    public String serialize(@NonNull T value) {
        return gson.toJson(value, clazz);
    }

    @NonNull
    @Override
    public T deserialize(@NonNull String serialized) {
        return gson.fromJson(serialized, clazz);
    }
}
