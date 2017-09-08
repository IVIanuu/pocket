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
import android.support.v4.util.LruCache;

import com.ivianuu.pocket.Cache;

/**
 * Simple lru cache implementation
 */
public final class PocketLruCache implements Cache {

    private final LruCache<String, Object> lruCache;
    private final SizePredicate sizePredicate;

    private PocketLruCache(int maxSize, final SizePredicate sizePredicate) {
        this.sizePredicate = sizePredicate;

        this.lruCache = new LruCache<String, Object>(maxSize) {
            @Override
            protected int sizeOf(String key, Object value) {
                if (sizePredicate != null) {
                    return sizePredicate.sizeOf(key, value);
                } else {
                    return super.sizeOf(key, value);
                }
            }
        };
    }

    /**
     * Returns a new pocket lru cache
     * This will use 1 as the size for every item
     */
    @NonNull
    public static Cache create(int maxSize) {
        return create(maxSize, null);
    }

    /**
     * Returns a new pocket lru cache
     */
    @NonNull
    public static Cache create(int maxSize, @Nullable SizePredicate sizePredicate) {
        return new PocketLruCache(maxSize, sizePredicate);
    }

    @Override
    public <T> void put(@NonNull String key, @NonNull T value) {
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

    public interface SizePredicate {
        /**
         * Returns the size of this value
         */
        int sizeOf(@NonNull String key, @NonNull Object value);
    }
}
