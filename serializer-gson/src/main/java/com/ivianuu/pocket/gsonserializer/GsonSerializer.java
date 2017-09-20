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

package com.ivianuu.pocket.gsonserializer;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.ivianuu.pocket.Serializer;

import java.lang.reflect.Type;

/**
 * Gson serializer implementation
 */
public final class GsonSerializer implements Serializer {

    private final Gson gson;

    private GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    /**
     * Returns a gson serializer
     * This will create a new gson instance
     */
    @NonNull
    public static Serializer create() {
        return create(new Gson());
    }

    /**
     * Returns a gson serializer with a custom gson instance
     */
    @NonNull
    public static Serializer create(@NonNull Gson gson) {
        return new GsonSerializer(gson);
    }

    @NonNull
    @Override
    public String serialize(@NonNull Object value) {
        return gson.toJson(value);
    }

    @NonNull
    @Override
    public <T> T deserialize(@NonNull String serialized, @NonNull Type type) throws Exception {
        return gson.fromJson(serialized, type);
    }
}
