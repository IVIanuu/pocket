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

package com.ivianuu.pocket.moshiserializer;

import android.support.annotation.NonNull;

import com.ivianuu.pocket.Serializer;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Moshi serializer implementation
 */
public final class MoshiSerializer implements Serializer {

    private final Moshi moshi;

    private MoshiSerializer(Moshi moshi) {
        this.moshi = moshi;
    }

    /**
     * Returns a new moshi serializer
     */
    @NonNull
    public static Serializer create() {
        return create(new Moshi.Builder().build());
    }

    /**
     * Returns a new moshi serializer
     */
    @NonNull
    public static Serializer create(@NonNull Moshi moshi) {
        return new MoshiSerializer(moshi);
    }

    @NonNull
    @Override
    public String serialize(@NonNull Object value) {
        JsonAdapter<Object> adapter = moshi.<Object>adapter(value.getClass());
        return adapter.toJson(value);
    }

    @NonNull
    @Override
    public <T> T deserialize(@NonNull String serialized, @NonNull Class<T> clazz) throws Exception {
        JsonAdapter<T> adapter = moshi.adapter(clazz);
        //noinspection ConstantConditions
        return adapter.fromJson(serialized);
    }
}
