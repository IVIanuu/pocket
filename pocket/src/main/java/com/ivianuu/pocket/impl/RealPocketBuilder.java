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

import com.ivianuu.pocket.Cache;
import com.ivianuu.pocket.Encryption;
import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.Serializer;
import com.ivianuu.pocket.Storage;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Generic pocket builder
 */
public final class RealPocketBuilder {

    RealPocketBuilder() {}

    private Cache cache;
    private Encryption encryption;
    private Storage storage;
    private Serializer serializer;
    private Scheduler scheduler;

    /**
     * Sets the cache
     */
    @NonNull
    public RealPocketBuilder cache(@NonNull Cache cache) {
        this.cache = cache;
        return this;
    }

    /**
     * Sets the encryption
     */
    @NonNull
    public RealPocketBuilder encryption(@NonNull Encryption encryption) {
        this.encryption = encryption;
        return this;
    }

    /**
     * Sets the storage
     */
    @NonNull
    public RealPocketBuilder storage(@NonNull Storage storage) {
        this.storage = storage;
        return this;
    }

    /**
     * Sets the serializer
     */
    @NonNull
    public RealPocketBuilder serializer(@NonNull Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /**
     * The default scheduler
     */
    @NonNull
    public RealPocketBuilder scheduler(@NonNull Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Returns a new pocket instance
     */
    @NonNull
    public Pocket build() {
        // check required modules
        if (storage == null) {
            throw new IllegalStateException("storage must be set");
        }
        if (serializer == null) {
            throw new IllegalStateException("serializer must be set");
        }

        // check optionals
        if (cache == null) {
            cache = NoOpCache.create();
        }
        if (encryption == null) {
            encryption = NoOpEncryption.create();
        }
        if (scheduler == null) {
            scheduler = Schedulers.io();
        }

        return new RealPocket(cache, encryption, storage, serializer, scheduler);
    }
}
