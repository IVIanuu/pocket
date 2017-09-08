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
import android.support.annotation.Nullable;

/**
 * Pocket cache
 */
public interface Cache {

    /**
     * Puts the value into the cache
     */
    <T> void put(@NonNull String key, @NonNull T value);

    /**
     * Returns the value from the cache or null if not exists
     */
    @Nullable
    <T> T get(@NonNull String key);

    /**
     * Removes the key from the cache
     */
    void remove(@NonNull String key);

    /**
     * Removes all values from the cache
     */
    void removeAll();
}
