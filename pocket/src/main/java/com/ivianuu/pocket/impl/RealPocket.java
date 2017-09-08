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
import android.support.v4.util.Pair;

import com.ivianuu.pocket.Encryption;
import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.Serializer;
import com.ivianuu.pocket.Storage;

import java.util.HashMap;
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
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;

/**
 * RealPocket dao
 */
final class RealPocket implements Pocket {

    private final Encryption encryption;
    private final Storage storage;
    private final Serializer serializer;
    private final Scheduler scheduler;

    private final PublishProcessor<String> keyChangesProcessor = PublishProcessor.create();

    RealPocket(@NonNull Encryption encryption,
               @NonNull Storage storage,
               @NonNull Serializer serializer,
               @NonNull Scheduler scheduler) {
        this.encryption = encryption;
        this.storage = storage;
        this.serializer = serializer;
        this.scheduler = scheduler;
    }

    @NonNull
    @Override
    public <T> Completable put(@NonNull final String key, @NonNull final T value) {
        return Completable
                .fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        // serialize
                        String serialized = serializer.serialize(value);

                        // add meta to deserialize without the need to put the class arg
                        String meta = ClassMetaUtil.pack(serialized, value.getClass());

                        // encrypt
                        String encrypted = encryption.encrypt(key, meta);

                        // persist
                        storage.put(key, encrypted);
                        return new Object();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        keyChangesProcessor.onNext(key);
                    }
                }).subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public <T> Maybe<T> get(@NonNull final String key) {
        return Maybe.create(new MaybeOnSubscribe<T>() {
            @Override
            public void subscribe(MaybeEmitter<T> e) throws Exception {
                // get encrypted data
                String encrypted = storage.get(key);

                // null check
                if (encrypted != null) {
                    // decrypt to meta
                    String meta = encryption.decrypt(key, encrypted);

                    // retrieve meta values
                    Pair<String, Class<?>> metaPair = ClassMetaUtil.unpack(meta);

                    // deserialize to the value
                    T value = serializer.deserialize(metaPair.first, metaPair.second);

                    // notify
                    if (!e.isDisposed()) {
                        e.onSuccess(value);
                    }
                }

                if (!e.isDisposed()) {
                    e.onComplete();
                }
            }
        }).subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public <T> Single<T> get(@NonNull String key, @NonNull T defaultValue) {
        // TODO: 08.09.2017 fix
        return (Single<T>) get(key)
                .defaultIfEmpty(defaultValue)
                .toSingle();
    }

    @CheckResult @NonNull
    @Override
    public Completable delete(@NonNull final String key) {
        return Completable
                .fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        storage.delete(key);
                        return new Object();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        keyChangesProcessor.onNext(key);
                    }
                })
                .subscribeOn(scheduler);
    }

    @NonNull
    @Override
    public Completable deleteAll() {
        return getAllKeys()
                .doOnSuccess(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> keys) throws Exception {
                        storage.deleteAll();
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
    public Single<HashMap<String, Object>> getAllValues() {
        return Single.create(new SingleOnSubscribe<HashMap<String, Object>>() {
            @Override
            public void subscribe(SingleEmitter<HashMap<String, Object>> e) throws Exception {
                List<String> keys = getAllKeys().blockingGet();
                HashMap<String, Object> map = new HashMap<>();
                for (String key : keys) {
                    if (e.isDisposed()) {
                        // stop if the observer is not interested anymore
                        return;
                    }

                    Object value = get(key).blockingGet();
                    map.put(key, value);
                }

                if (!e.isDisposed()) {
                    e.onSuccess(map);
                }
            }
        });
    }

    @CheckResult @NonNull
    @Override
    public Flowable<String> keyChanges() {
        return keyChangesProcessor.share();
    }

    @CheckResult @NonNull
    @Override
    public <T> Flowable<T> latest(@NonNull final String key) {
        return keyChanges()
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String changedKey) throws Exception {
                        return changedKey.equals(key);
                    }
                })
                .startWith("<init>") // Dummy value to trigger initial load.
                .flatMapMaybe(new Function<String, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(String s) throws Exception {
                        return get(key);
                    }
                });
    }

    @CheckResult @NonNull
    @Override
    public Flowable<Pair<String, Object>> updates() {
        return keyChanges()
                .flatMapMaybe(new Function<String, MaybeSource<? extends Pair<String, Object>>>() {
                    @Override
                    public MaybeSource<? extends Pair<String, Object>> apply(final String key) throws Exception {
                        return get(key)
                                .map(new Function<Object, Pair<String, Object>>() {
                                    @Override
                                    public Pair<String, Object> apply(Object value) throws Exception {
                                        return new Pair<>(key, value);
                                    }
                                });
                    }
                });
    }
}
