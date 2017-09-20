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

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivianuu.pocket.Cache;
import com.ivianuu.pocket.Encryption;
import com.ivianuu.pocket.InternalPocket;
import com.ivianuu.pocket.Serializer;
import com.ivianuu.pocket.Storage;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

/**
 * Real internal pocket implementation
 */
final class RealInternalPocket implements InternalPocket {

    private final Cache cache;
    private final Encryption encryption;
    private final Storage storage;
    private final Serializer serializer;
    private final Scheduler scheduler;

    private final PublishProcessor<String> keyChangesProcessor = PublishProcessor.create();

    RealInternalPocket(@NonNull Cache cache,
                       @NonNull Encryption encryption,
                       @NonNull Storage storage,
                       @NonNull Serializer serializer,
                       @Nullable Scheduler scheduler) {
        this.cache = cache;
        this.encryption = encryption;
        this.storage = storage;
        this.serializer = serializer;
        this.scheduler = scheduler;
    }

    @CheckResult
    @NonNull
    @Override
    public Completable put(@NonNull final String key, @NonNull final Object value) {
        Completable completable = Completable.fromCallable(() -> {
            // serialize
            String serialized = serializer.serialize(value);

            // encrypt
            String encrypted = encryption.encrypt(key, serialized);

            // persist
            storage.put(key, encrypted);
            return new Object();
        }).doOnComplete(() -> {
            // put into the cache
            cache.put(key, value);
            // notify update
            keyChangesProcessor.onNext(key);
        });
        if (scheduler != null) {
            completable = completable.subscribeOn(scheduler);
        }

        return completable;
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Maybe<T> get(@NonNull final String key, @NonNull final Class<T> clazz) {
        return get(key, (Type) clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Maybe<T> get(@NonNull final String key, @NonNull final Type type) {
        Maybe<T> maybe = this.<T>cached(key)
                .switchIfEmpty(Maybe.<T>create(e -> {
                    // get encrypted data
                    String encrypted = storage.get(key);

                    // null check
                    if (encrypted != null) {
                        // decrypt to serialized
                        String serialized = encryption.decrypt(key, encrypted);

                        // deserialize to the value
                        T value = serializer.deserialize(serialized, type);

                        // notify
                        if (!e.isDisposed()) {
                            e.onSuccess(value);
                        }
                    }

                    if (!e.isDisposed()) {
                        e.onComplete();
                    }
                }).doOnSuccess(value -> {
                    // put into the cache
                    cache.put(key, value);
                }));

        // first try to get the cached value
        if (scheduler != null) {
            maybe = maybe.subscribeOn(scheduler);
        }

        return maybe;
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<T> get(@NonNull String key, @NonNull T defaultValue, @NonNull Class<T> clazz) {
        return get(key, defaultValue, (Type) clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<T> get(@NonNull String key, @NonNull T defaultValue, @NonNull Type type) {
        Single<T> single = this.<T>get(key, type)
                .defaultIfEmpty(defaultValue) // switch to default
                .toSingle();
        if (scheduler != null) {
            single = single.subscribeOn(scheduler);
        }

        return single;
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<Option<T>> getOptional(@NonNull String key, @NonNull Class<T> clazz) {
        return getOptional(key, (Type) clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<Option<T>> getOptional(@NonNull String key, @NonNull Type type) {
        return this.<T>get(key, type)
                .map(Option::of)
                .switchIfEmpty(Maybe.just(Option.absent()))
                .toSingle();
    }

    @CheckResult
    @NonNull
    @Override
    public Completable delete(@NonNull final String key) {
        Completable completable = Completable
                .fromCallable(() -> {
                    // delete from storage
                    storage.delete(key);
                    return new Object();
                })
                .doOnComplete(() -> {
                    // remove from cache
                    cache.remove(key);
                    // notify change
                    keyChangesProcessor.onNext(key);
                });
        if (scheduler != null) {
            completable = completable.subscribeOn(scheduler);
        }
        return completable;
    }

    @CheckResult
    @NonNull
    @Override
    public Completable deleteAll() {
        // first get all keys
        // we need the keys to proper update the key changes latest
        Completable completable = getAllKeys()
                .doOnSuccess(keys -> {
                    // clear storage
                    storage.deleteAll();
                    // clear cache
                    cache.removeAll();
                    // notify change
                    for (String key : keys) {
                        keyChangesProcessor.onNext(key);
                    }
                })
                .toCompletable();
        if (scheduler != null) {
            completable = completable.subscribeOn(scheduler);
        }
        return completable;
    }

    @CheckResult
    @NonNull
    @Override
    public Single<Boolean> contains(@NonNull final String key) {
        Single<Boolean> single = Single.fromCallable(() -> storage.contains(key));
        if (scheduler != null) {
            single = single.subscribeOn(scheduler);
        }

        return single;
    }

    @CheckResult
    @NonNull
    @Override
    public Single<List<String>> getAllKeys() {
        Single<List<String>> single = Single.fromCallable(storage::getAllKeys);
        if (scheduler != null) {
            single = single.subscribeOn(scheduler);
        }
        return single;
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Single<Map<String, T>> getAll(Class<T> clazz) {
        return getAll((Type) clazz);
    }

    @CheckResult @NonNull
    @Override
    public <T> Single<Map<String, T>> getAll(final Type type) {
        Single<Map<String, T>> single = getAllKeys()
                .map(keys -> {
                    Map<String, T> map = new LinkedHashMap<>();
                    // TODO: 09.09.2017  dirtyyyy
                    for (String key : keys) {
                        try {
                            map.put(key, this.<T>get(key, type).blockingGet());
                        } catch (Exception ignored) {}
                    }

                    return map;
                });
        if (scheduler != null) {
            single = single.subscribeOn(scheduler);
        }

        return single;
    }

    @CheckResult
    @NonNull
    @Override
    public Single<Integer> getCount() {
        Single<Integer> single = Single.fromCallable(storage::getCount);
        if (scheduler != null) {
            single = single.subscribeOn(scheduler);
        }

        return single;
    }

    @CheckResult
    @NonNull
    @Override
    public Flowable<String> keyChanges() {
        Flowable<String> flowable = keyChangesProcessor.hide().share();
        if (scheduler != null) {
            flowable = flowable.subscribeOn(scheduler);
        }
        return flowable;
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Map.Entry<String, T>> stream(@NonNull Class<T> clazz) {
        return stream((Type) clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Map.Entry<String, T>> stream(@NonNull final Type type) {
        Flowable<Map.Entry<String, T>> flowable = keyChanges()
                .flatMapMaybe(key -> {
                    // TODO: 09.09.2017 is there a better way ?
                    try {
                        T value = this.<T>get(key, type).blockingGet();
                        Map.Entry<String, T> entry = new AbstractMap.SimpleEntry<>(key, value);
                        return Maybe.just(entry);
                    } catch (Exception ignored) {
                        return Maybe.empty();
                    }
                });
        if (scheduler != null) {
            flowable = flowable.subscribeOn(scheduler);
        }

        return flowable;
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Option<T>> stream(@NonNull final String key, @NonNull final Class<T> clazz) {
        return stream(key, (Type) clazz);
    }

    @CheckResult
    @NonNull
    @Override
    public <T> Flowable<Option<T>> stream(@NonNull final String key, @NonNull final Type type) {
        // we need to filter the key changes were interested in
        // every time the key changes we emit the next value
        Flowable<Option<T>> flowable = this.<T>stream(type)
                .filter(entry -> entry.getKey().equals(key))
                .startWith(new AbstractMap.SimpleEntry<>(key, null))
                .map(entry -> {
                    if (entry.getValue() != null) {
                        return Option.of(entry.getValue());
                    } else {
                        return Option.absent();
                    }
                });
        if (scheduler != null) {
            flowable = flowable.subscribeOn(scheduler);
        }

        return flowable;
    }

    @NonNull
    @Override
    public <T> Maybe<T> cached(@NonNull String key) {
        final T cachedValue = cache.get(key);
        if (cachedValue != null) {
            return Maybe.just(cachedValue);
        } else {
            return Maybe.empty();
        }
    }
}
