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

package com.ivianuu.pocket.moshiserializer

import com.ivianuu.pocket.Parser
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import java.lang.reflect.Type

/**
 * A [Parser] which uses [Moshi] under the hood
 */
class MoshiParser<T> private constructor(private val adapter: JsonAdapter<T>) : Parser<T> {

    override fun toJson(value: T): String = adapter.toJson(value)

    override fun fromJson(serialized: String): T = adapter.fromJson(serialized)!!

    companion object {

        /**
         * Returns a new [Parser]
         */
        inline fun <reified T> create(): Parser<T> = create(T::class.java)

        /**
         * Returns a new [Parser]
         */
        @JvmStatic
        fun <T> create(type: Type): Parser<T> = create(Moshi.Builder().build(), type)

        /**
         * Returns a new [Parser]
         */
        @JvmStatic
        inline fun <reified T> create(moshi: Moshi): Parser<T> = create(moshi, T::class.java)

        /**
         * Returns a new [Parser]
         */
        @JvmStatic
        fun <T> create(
            moshi: Moshi,
            type: Type
        ): Parser<T> = create(moshi.adapter(type))

        /**
         * Returns a new [Parser]
         */
        @JvmStatic
        fun <T> create(adapter: JsonAdapter<T>): Parser<T> = MoshiParser(adapter)
    }
}
