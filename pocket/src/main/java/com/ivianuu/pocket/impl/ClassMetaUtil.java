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
import android.support.v4.util.Pair;

/**
 * Class meta helper
 */
final class ClassMetaUtil {

    private static final String DELIMITER = "=:=";

    private ClassMetaUtil() {
        // no instances
    }

    @NonNull
    static String pack(@NonNull String serialized, @NonNull Class<?> clazz) {
        return serialized + DELIMITER + clazz.getName();
    }

    @NonNull
    static Pair<String, Class<?>> unpack(@NonNull String meta) throws ClassNotFoundException {
        String[] splitted = meta.split(DELIMITER);
        String serialized = splitted[0];
        Class<?> clazz = Class.forName(splitted[1]);
        return new Pair<String, Class<?>>(serialized, clazz);
    }
}
