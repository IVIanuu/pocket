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

package com.ivianuu.pocket.kotlin

import com.ivianuu.pocket.Cache
import com.ivianuu.pocket.Encryption
import com.ivianuu.pocket.Serializer
import com.ivianuu.pocket.Storage
import io.reactivex.Scheduler

/**
 * Fluent pocket builder
 */
class FluentPocketBuilder private constructor() {

    companion object {

        fun builder(cache: Cache? = null,
                    encryption: Encryption? = null,
                    scheduler : Scheduler? = null,
                    serializer: Serializer,
                    storage: Storage) : FluentRealPocketBuilder {
            return FluentRealPocketBuilder(cache, encryption, scheduler, serializer, storage)
        }
    }
}