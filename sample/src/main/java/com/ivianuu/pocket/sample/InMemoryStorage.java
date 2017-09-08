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

package com.ivianuu.pocket.sample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivianuu.pocket.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * In memory storage
 */
public class InMemoryStorage implements Storage {

    private final HashMap<String, String> map = new HashMap<>();

    @Override
    public void put(@NonNull String key, @NonNull String value) {
        map.put(key, value);
    }

    @Nullable
    @Override
    public String get(@NonNull String key) {
        return map.get(key);
    }

    @Override
    public void delete(@NonNull String key) {
        map.remove(key);
    }

    @Override
    public void deleteAll() {
        map.clear();
    }

    @Override
    public boolean contains(@NonNull String key) {
        return map.containsKey(key);
    }

    @NonNull
    @Override
    public List<String> getAllKeys() {
        return new ArrayList<>(map.keySet());
    }
}
