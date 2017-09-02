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

package com.ivianuu.pocket;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

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
class RealPocket<T> implements Pocket<T> {

    private final Scheduler scheduler;
    private final Storage<T> storage;

    private final PublishProcessor<String> keyChangesProcessor = PublishProcessor.create();

    RealPocket(@NonNull Context context,
               @NonNull String name,
               @NonNull Scheduler scheduler,
               @NonNull Gson gson,
               @NonNull Class<T> clazz) {
        this.scheduler = scheduler;
        this.storage = new RealStorage<>(context, name, gson, clazz);
    }

    @CheckResult @NonNull
    @Override
    public Completable destroy() {
        return getAllKeys()
                .doOnSuccess(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> keys) throws Exception {
                        storage.destroy();
                        for (String key : keys) {
                            keyChangesProcessor.onNext(key);
                        }
                    }
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @CheckResult @NonNull
    @Override
    public Completable write(@NonNull final String key, @NonNull final T value) {
        return Completable
                .fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        storage.insert(key, value);
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

    @CheckResult @NonNull
    @Override
    public Maybe<T> read(@NonNull final String key) {
        return Maybe.create(new MaybeOnSubscribe<T>() {
            @Override
            public void subscribe(MaybeEmitter<T> e) throws Exception {
                T value = storage.select(key);
                if (value != null) {
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

    @CheckResult @NonNull
    @Override
    public Single<T> read(@NonNull final String key, @NonNull final T defaultValue) {
        return Single.create(new SingleOnSubscribe<T>() {
            @Override
            public void subscribe(SingleEmitter<T> e) throws Exception {
                T value = storage.select(key);
                if (!e.isDisposed()) {
                    e.onSuccess(value != null ? value : defaultValue);
                }
            }
        }).subscribeOn(scheduler);
    }

    @CheckResult @NonNull
    @Override
    public Single<Boolean> exists(@NonNull final String key) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return storage.exist(key);
            }
        }).subscribeOn(scheduler);
    }

    @CheckResult @NonNull
    @Override
    public Single<Long> lastModified(@NonNull final String key) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return storage.lastModified(key);
            }
        }).subscribeOn(scheduler);
    }

    @CheckResult @NonNull
    @Override
    public Completable delete(@NonNull final String key) {
        return Completable
                .fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        storage.deleteIfExists(key);
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
    public Single<HashMap<String, T>> getAllValues() {
        return Single.create(new SingleOnSubscribe<HashMap<String, T>>() {
            @Override
            public void subscribe(SingleEmitter<HashMap<String, T>> e) throws Exception {
                List<String> keys = getAllKeys().blockingGet();
                HashMap<String, T> map = new HashMap<>();
                for (String key : keys) {
                    if (e.isDisposed()) {
                        // stop if the observer is not interested anymore
                        return;
                    }

                    T value = read(key).blockingGet();
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
    public Flowable<T> latest(@NonNull final String key) {
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
                        return read(key);
                    }
                });
    }

    @CheckResult @NonNull
    @Override
    public Flowable<Pair<String, T>> updates() {
        return keyChanges()
                .flatMapMaybe(new Function<String, MaybeSource<? extends Pair<String, T>>>() {
                    @Override
                    public MaybeSource<? extends Pair<String, T>> apply(final String key) throws Exception {
                        return read(key)
                                .map(new Function<T, Pair<String,T>>() {
                                    @Override
                                    public Pair<String, T> apply(T value) throws Exception {
                                        return new Pair<>(key, value);
                                    }
                                });
                    }
                });
    }
}
