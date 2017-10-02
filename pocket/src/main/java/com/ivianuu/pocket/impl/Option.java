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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.ivianuu.pocket.Preconditions.checkNotNull;

/**
 * Option
 */
public final class Option<T> {

    private final T value;

    private Option(T value) {
        this.value = value;
    }

    /**
     * Returns a new present option
     */
    @NonNull
    public static <T> Option<T> of(@NonNull T value) {
        checkNotNull(value, "value == null");
        return new Option<>(value);
    }

    /**
     * Returns a new present option
     */
    @NonNull
    public static <T> Option<T> absent() {
        return new Option<>(null);
    }

    /**
     * Returns the value
     * Only non null if present returns true
     */
    @Nullable
    public T get() {
        return value;
    }

    /**
     * Returns whether the option is present
     */
    public boolean present() {
        return value != null;
    }
}
