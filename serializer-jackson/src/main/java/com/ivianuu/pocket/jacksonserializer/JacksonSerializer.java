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

package com.ivianuu.pocket.jacksonserializer;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ivianuu.pocket.Serializer;

import java.lang.reflect.Type;

/**
 * Jackson serializer implementation
 */
public final class JacksonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    private JacksonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a new jackson serializer
     */
    @NonNull
    public static Serializer create() {
        return create(new ObjectMapper());
    }

    /**
     * Returns a new jackson serializer
     */
    @NonNull
    public static Serializer create(@NonNull ObjectMapper objectMapper) {
        return new JacksonSerializer(objectMapper);
    }

    @NonNull
    @Override
    public String serialize(@NonNull Object value) throws Exception {
        JavaType javaType = objectMapper.constructType(value.getClass());
        ObjectWriter writer = objectMapper.writerFor(javaType);
        return writer.writeValueAsString(value);
    }

    @NonNull
    @Override
    public <T> T deserialize(@NonNull String serialized, @NonNull Type type) throws Exception {
        JavaType javaType = objectMapper.constructType(type);
        ObjectReader reader = objectMapper.readerFor(javaType);
        return reader.readValue(serialized);
    }
}
