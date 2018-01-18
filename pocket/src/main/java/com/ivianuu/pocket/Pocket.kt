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

package com.ivianuu.pocket

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Pocket dao
 */
interface Pocket<T> {

    /**
     * Persists the [value] with the [key]
     */
    fun put(key: String, value: T): Completable

    /**
     * Returns the [T] for the [key] or [Maybe.empty]
     */
    fun get(key: String): Maybe<T>

    /**
     * Returns the [T] for the [key] or if not present the [defaultValue]
     */
    fun get(key: String, defaultValue: T): Single<T>

    /**
     * Returns the wrapped [T] for the [key] or [Option.empty] if not present
     * if no value found it will return the default value
     */
    fun getOptional(key: String): Single<Option<T>>

    /**
     * Returns all [T]'s of this [Pocket]
     */
    fun getAll(): Single<List<T>>

    /**
     * Returns all keys of this [Pocket]
     */
    fun getAllKeys(): Single<List<String>>

    /**
     * Returns the count of persisted [T]'s
     */
    fun getCount(): Single<Int>

    /**
     * Deletes the [T] for the [key]
     */
    fun delete(key: String): Completable

    /**
     * Deletes all values of this [Pocket]
     */
    fun deleteAll(): Completable

    /**
     * Returns whether a value for the [key] exists or not
     */
    fun contains(key: String): Single<Boolean>

    /**
     * Emits on key changes
     */
    fun keyChanges(): Observable<String>

    /**
     * Emits the [T] of [key] on changes and on first subscribe
     */
    fun stream(key: String): Observable<Option<T>>

    /**
     * Emits all [T]'s on changes and on first subscribe
     */
    fun streamAll(): Observable<List<T>>
}