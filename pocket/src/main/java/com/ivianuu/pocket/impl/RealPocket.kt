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

import com.ivianuu.pocket.*
import io.reactivex.*

import io.reactivex.subjects.PublishSubject

/**
 * Real implementation of a [Pocket] which can be subclassed
 */
open class RealPocket<T>(
    private val encryption: Encryption = NoEncryption,
    private val parser: Parser<T>,
    private val storage: Storage,
    private val scheduler: Scheduler?
) : Pocket<T> {

    private val keyChangesSubject = PublishSubject.create<String>()

    override fun put(key: String, value: T): Completable {
        return Completable
            .fromCallable {
                // serialize
                val serialized = parser.toJson(value)

                // encrypt
                val encrypted = encryption.encrypt(key, serialized)

                // persist
                storage.put(key, encrypted)
                Unit
            }
            // notify change
            .doOnComplete { keyChangesSubject.onNext(key) }
            .maybeSubscribeOn(scheduler)
    }


    override fun get(key: String): Maybe<T> {
        return Maybe
            .create<T> { e ->
                // get persisted data
                storage.get(key)?.let { persisted ->
                    // decrypt
                    val decrypted = encryption.decrypt(key, persisted)

                    // deserialize
                    val value = parser.fromJson(decrypted)

                    // notify
                    if (!e.isDisposed) {
                        e.onSuccess(value)
                    }
                }

                if (!e.isDisposed) {
                    e.onComplete()
                }
            }
            .maybeSubscribeOn(scheduler)
    }

    override fun get(key: String, defaultValue: T): Single<T> {
        return get(key)
            .defaultIfEmpty(defaultValue) // switch to default
            .toSingle()
            .maybeSubscribeOn(scheduler)
    }

    override fun getOptional(key: String): Single<Option<T>> {
        return get(key)
            .map { Option.of(it) }
            .defaultIfEmpty(Option.empty())
            .toSingle()
            .maybeSubscribeOn(scheduler)
    }

    override fun delete(key: String): Completable {
        return Completable
            .fromCallable {
                // delete from storage
                storage.delete(key)
                Unit
            }
            // notify change
            .doOnComplete { keyChangesSubject.onNext(key) }
            .maybeSubscribeOn(scheduler)
    }

    override fun deleteAll(): Completable {
        // first get all keys
        // we need the keys to proper update the key changes latest
        return getAllKeys()
            .doOnSuccess { keys ->
                // clear storage
                storage.deleteAll()
                // notify change
                for (key in keys) {
                    keyChangesSubject.onNext(key)
                }
            }
            .toCompletable()
            .maybeSubscribeOn(scheduler)
    }

    override fun contains(key: String): Single<Boolean> {
        return Single
            .fromCallable { storage.contains(key) }
            .maybeSubscribeOn(scheduler)
    }

    override fun getAllKeys(): Single<List<String>> {
        return Single
            .fromCallable { storage.getAllKeys() }
            .maybeSubscribeOn(scheduler)
    }

    override fun getAll(): Single<List<T>> {
        return getAllKeys()
            .flatMap { keys ->
                Observable.fromIterable(keys)
                    .flatMapMaybe { get(it) }
                    .toList()
            }
            .maybeSubscribeOn(scheduler)
    }

    override fun getCount(): Single<Int> {
        return getAllKeys()
            .map { it.size }
            .maybeSubscribeOn(scheduler)
    }

    override fun keyChanges(): Observable<String> {
        return keyChangesSubject
            .share()
            .maybeSubscribeOn(scheduler)
    }

    override fun stream(key: String): Observable<Option<T>> {
        return keyChanges()
            .startWith(key)
            .filter { it == key }
            .flatMapSingle { getOptional(it) }
            .maybeSubscribeOn(scheduler)
    }

    override fun streamAll(): Observable<List<T>> {
        return keyChanges()
            .startWith("")
            .flatMapSingle { getAll() }
            .maybeSubscribeOn(scheduler)
    }
}

private fun Completable.maybeSubscribeOn(scheduler: Scheduler?): Completable {
    scheduler?.let { return this.subscribeOn(it) }
    return this
}

private fun <T> Maybe<T>.maybeSubscribeOn(scheduler: Scheduler?): Maybe<T> {
    scheduler?.let { return this.subscribeOn(it) }
    return this
}

private fun <T> Observable<T>.maybeSubscribeOn(scheduler: Scheduler?): Observable<T> {
    scheduler?.let { return this.subscribeOn(it) }
    return this
}

private fun <T> Single<T>.maybeSubscribeOn(scheduler: Scheduler?): Single<T> {
    scheduler?.let { return this.subscribeOn(it) }
    return this
}