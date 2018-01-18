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

package com.ivianuu.pocket.impl

import com.ivianuu.pocket.Encryption
import com.ivianuu.pocket.Parser
import com.ivianuu.pocket.Pocket
import com.ivianuu.pocket.Storage
import io.reactivex.Scheduler

/**
 * [Pocket] builder
 */
class PocketBuilder<T> private constructor() {

    private var encryption: Encryption? = null
    private lateinit var parser: Parser<T>
    private var scheduler: Scheduler? = null
    private lateinit var storage: Storage

    /**
     * Sets the [Encryption]
     */
    fun encryption(encryption: Encryption): PocketBuilder<T> {
        this.encryption = encryption
        return this
    }

    /**
     * Sets the [Parser]
     */
    fun parser(parser: Parser<T>): PocketBuilder<T> {
        this.parser = parser
        return this
    }

    /**
     * The default [Scheduler]
     */
    fun scheduler(scheduler: Scheduler): PocketBuilder<T> {
        this.scheduler = scheduler
        return this
    }

    /**
     * Sets the [Storage]
     */
    fun storage(storage: Storage): PocketBuilder<T> {
        this.storage = storage
        return this
    }

    /**
     * Returns a new [Pocket]
     */
    fun build(): Pocket<T> =
        RealPocket(
            encryption = encryption ?: NoEncryption,
            parser = parser,
            scheduler = scheduler,
            storage = storage)

    companion object {
        /**
         * Returns a new [PocketBuilder]
         */
        fun <T> newBuilder() = PocketBuilder<T>()
    }
}
