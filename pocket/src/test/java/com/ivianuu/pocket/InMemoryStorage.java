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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * In memory storage
 */
class InMemoryStorage implements Storage {

    private final HashMap<String, String> storage = new HashMap<>();

    @Override
    public void put(@NonNull String key, @NonNull String value) {
        storage.put(key, value);
    }

    @Nullable
    @Override
    public String get(@NonNull String key) {
        return storage.get(key);
    }

    @Override
    public void delete(@NonNull String key) {
        storage.remove(key);
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    @Override
    public boolean contains(@NonNull String key) {
        return storage.containsKey(key);
    }

    @NonNull
    @Override
    public List<String> getAllKeys() {
        return new ArrayList<>(storage.keySet());
    }

    @Override
    public int getCount() {
        return storage.size();
    }
}
