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

import java.lang.reflect.Type;

/**
 * Serializes and deserializes objects
 */
public interface Serializer {

    /**
     * Serializes the value
     */
    @NonNull
    <T> String serialize(@NonNull T value);

    /**
     * Deserializes the string to the value
     */
    @NonNull
    <T> T deserialize(@NonNull String serialized, @NonNull Type type) throws Exception;
}