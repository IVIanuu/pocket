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

package com.ivianuu.pocket.lrucache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivianuu.pocket.Cache;

import static com.ivianuu.preconditions.Preconditions.checkNotNull;

/**
 * Simple lru cache implementation
 */
public final class LruCache implements Cache {

    private final android.support.v4.util.LruCache<String, Object> lruCache;

    private LruCache(android.support.v4.util.LruCache<String, Object> lruCache) {
        this.lruCache = lruCache;
    }

    /**
     * Returns a new pocket lru cache
     */
    @NonNull
    public static Cache create(int maxSize) {
        return create(new android.support.v4.util.LruCache<>(maxSize));
    }

    /**
     * Returns a new pocket lru cache
     */
    @NonNull
    public static Cache create(@NonNull android.support.v4.util.LruCache<String, Object> lruCache) {
        checkNotNull(lruCache, "lruCache == null");
        return new LruCache(lruCache);
    }

    @Override
    public void put(@NonNull String key, @NonNull Object value) {
        lruCache.put(key, value);
    }

    @Nullable
    @Override
    public <T> T get(@NonNull String key) {
        return (T) lruCache.get(key);
    }

    @Override
    public void remove(@NonNull String key) {
        lruCache.remove(key);
    }

    @Override
    public void removeAll() {
        lruCache.evictAll();
    }
}
