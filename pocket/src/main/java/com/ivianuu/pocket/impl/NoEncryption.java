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

import com.ivianuu.pocket.Encryption;

/**
 * No op encryption
 */
final class NoEncryption implements Encryption {

    private NoEncryption() {}

    /**
     * Returns a new no op encryption
     */
    @NonNull
    static Encryption create() {
        return new NoEncryption();
    }

    @NonNull
    @Override
    public String encrypt(@NonNull String key, @NonNull String value) {
        return value;
    }

    @NonNull
    @Override
    public String decrypt(@NonNull String key, @NonNull String encrypted) {
        return encrypted;
    }

}
