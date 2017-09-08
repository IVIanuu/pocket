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

import com.ivianuu.pocket.Cache;
import com.ivianuu.pocket.Encryption;
import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.Serializer;
import com.ivianuu.pocket.Storage;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.MaybeSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;

/**
 * RealPocket dao
 */
final class RealPocket implements Pocket {

    private final Cache cache;
    private final Encryption encryption;
    private final Storage storage;
    private final Serializer serializer;
    private final Scheduler scheduler;

    private final PublishProcessor<String> keyChangesProcessor = PublishProcessor.create();

    RealPocket(@NonNull Cache cache,
               @NonNull Encryption encryption,
               @NonNull Storage storage,
               @NonNull Serializer serializer,
               @NonNull Scheduler scheduler) {
        this.cache = cache;
        this.encryption = encryption;
        this.storage = storage;
        this.serializer = serializer;
        this.scheduler = scheduler;
    }

    @NonNull
    @Override
    public <T> Completable put(@NonNull final String key, @NonNull final T value, @NonNull Class<T> clazz) {
        return Completable
                .fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        // serialize
                        String serialized = serializer.serialize(value);

                        // encrypt
                        String encrypted = encryption.encrypt(key, serialized);

                        // persist
                        storage.put(key, encrypted);
                        return new Object();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        // put into the cache
                        cache.put(key, value);
                        // notify update
                        keyChangesProcessor.onNext(key);
                    }
                }).subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public <T> Maybe<T> get(@NonNull final String key, @NonNull final Class<T> clazz) {
        // first try to get the cached value
        final T cachedValue = cache.get(key);
        if (cachedValue != null) {
            return Maybe.just(cachedValue);
        } else {
            // if the cache does not contains the key try to fetch from disk
            return Maybe.create(new MaybeOnSubscribe<T>() {
                @Override
                public void subscribe(MaybeEmitter<T> e) throws Exception {
                    // get encrypted data
                    String encrypted = storage.get(key);

                    // null check
                    if (encrypted != null) {
                        // decrypt to serialized
                        String serialized = encryption.decrypt(key, encrypted);

                        // deserialize to the value
                        T value = serializer.deserialize(serialized, clazz);

                        // notify
                        if (!e.isDisposed()) {
                            e.onSuccess(value);
                        }
                    }

                    if (!e.isDisposed()) {
                        e.onComplete();
                    }
                }
            })
                    .doOnSuccess(new Consumer<T>() {
                        @Override
                        public void accept(T value) throws Exception {
                            // put into the cache afterwards
                            cache.put(key, value);
                        }
                    }).subscribeOn(scheduler);
        }
    }

    @NonNull
    @Override
    public <T> Single<T> get(@NonNull String key, @NonNull T defaultValue, @NonNull Class<T> clazz) {
        return get(key, clazz)
                .defaultIfEmpty(defaultValue) // switch to default
                .toSingle();
    }

    @CheckResult @NonNull
    @Override
    public Completable delete(@NonNull final String key) {
        return Completable
                .fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        // delete from storage
                        storage.delete(key);
                        return new Object();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        // remove from cache
                        cache.remove(key);
                        // notify change
                        keyChangesProcessor.onNext(key);
                    }
                })
                .subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public Completable deleteAll() {
        // first get all keys
        // we need the keys to proper update the key changes latest
        return getAllKeys()
                .doOnSuccess(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> keys) throws Exception {
                        // clear storage
                        storage.deleteAll();
                        // clear cache
                        cache.removeAll();
                        // notify change
                        for (String key : keys) {
                            keyChangesProcessor.onNext(key);
                        }
                    }
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public Single<Boolean> contains(@NonNull final String key) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return storage.contains(key);
            }
        }).subscribeOn(scheduler);
    }

    @CheckResult @NonNull
    @Override
    public Single<List<String>> getAllKeys() {
        return Single.fromCallable(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return storage.getAllKeys();
            }
        }).subscribeOn(scheduler);
    }

    @CheckResult @NonNull
    @Override
    public Flowable<String> keyChanges() {
        return keyChangesProcessor.share();
    }

    @NonNull
    @Override
    public <T> Flowable<T> stream(@NonNull final String key, @NonNull final Class<T> clazz) {
        // we need to filter the key changes were interested in
        // every time the key changes we emit the next value
        return keyChanges()
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String changedKey) throws Exception {
                        return changedKey.equals(key);
                    }
                })
                .startWith("") // Dummy value to trigger initial load.
                .flatMapMaybe(new Function<String, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(String s) throws Exception {
                        return get(key, clazz);
                    }
                });
    }
}
