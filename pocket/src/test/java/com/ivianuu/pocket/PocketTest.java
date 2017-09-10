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

import com.ivianuu.pocket.gsonserializer.GsonSerializer;
import com.ivianuu.pocket.impl.Option;
import com.ivianuu.pocket.impl.PocketBuilder;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.reactivex.subscribers.TestSubscriber;

/**
 * Pocket test
 */
public class PocketTest {

    private static final String TEST_KEY = "key";
    private static final String TEST_VALUE = "value";

    @Test
    public void testPut() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();

        Assertions.assertThat(pocket.getCount().blockingGet()).isEqualTo(1);
    }

    @Test
    public void testGet() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        Assertions.assertThat(pocket.get(TEST_KEY, String.class).blockingGet()).isNull();

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();

        Assertions.assertThat(pocket.get(TEST_KEY, String.class).blockingGet()).isEqualTo(TEST_VALUE);
    }

    @Test
    public void testGetWithDefault() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        String value = pocket.get(TEST_KEY, "default_value", String.class).blockingGet();
        Assertions.assertThat(value).isEqualTo("default_value");

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();
        value = pocket.get(TEST_KEY, "default_value", String.class).blockingGet();
        Assertions.assertThat(value).isEqualTo(TEST_VALUE);
    }

    @Test
    public void testGetOptional() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        Option<String> option = pocket.getOptional(TEST_KEY, String.class).blockingGet();
        Assertions.assertThat(option.present()).isEqualTo(false);

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();
        option = pocket.getOptional(TEST_KEY, String.class).blockingGet();
        Assertions.assertThat(option.present()).isEqualTo(true);
    }

    @Test
    public void testDelete() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();
        pocket.delete(TEST_KEY).blockingAwait();

        Assertions.assertThat(pocket.contains(TEST_KEY).blockingGet()).isEqualTo(false);
    }

    @Test
    public void testDeleteAll() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        pocket.put("1", TEST_VALUE).blockingAwait();
        pocket.put("2", TEST_VALUE).blockingAwait();
        pocket.put("3", TEST_VALUE).blockingAwait();

        Assertions.assertThat(pocket.getCount().blockingGet()).isEqualTo(3);

        pocket.deleteAll().blockingAwait();

        Assertions.assertThat(pocket.getCount().blockingGet()).isEqualTo(0);
    }

    @Test
    public void testContains() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        Assertions.assertThat(pocket.contains(TEST_VALUE).blockingGet()).isEqualTo(false);

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();

        Assertions.assertThat(pocket.contains(TEST_KEY).blockingGet()).isEqualTo(true);
    }

    @Test
    public void testGetAllKeys() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        pocket.put("A", TEST_VALUE).blockingAwait();
        pocket.put("B", TEST_VALUE).blockingAwait();
        pocket.put("C", TEST_VALUE).blockingAwait();

        Assertions.assertThat(pocket.getAllKeys().blockingGet()).containsExactly("A", "B", "C");
    }

    @Test
    public void testGetAll() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        pocket.put("A", true).blockingAwait();
        pocket.put("B", false).blockingAwait();
        pocket.put("C", 1234).blockingAwait();

        Assertions.assertThat(pocket.getAll(Boolean.class).blockingGet().size()).isEqualTo(2);
        Assertions.assertThat(pocket.getAll(Integer.class).blockingGet().size()).isEqualTo(1);
    }

    @Test
    public void testGetCount() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        pocket.put("A", TEST_VALUE).blockingAwait();
        Assertions.assertThat(pocket.getCount().blockingGet()).isEqualTo(1);
        pocket.put("B", TEST_VALUE).blockingAwait();
        Assertions.assertThat(pocket.getCount().blockingGet()).isEqualTo(2);
        pocket.delete("B").blockingAwait();
        Assertions.assertThat(pocket.getCount().blockingGet()).isEqualTo(1);
    }

    @Test
    public void testKeyChanges() {
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        pocket.keyChanges().subscribe(subscriber);

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();

        subscriber.assertValue(TEST_KEY);

        pocket.delete(TEST_KEY).blockingAwait();

        Assertions.assertThat(subscriber.valueCount()).isEqualTo(2);
    }

    @Test
    public void testStream() {
        /*
        Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(new InMemoryStorage())
                .build();

        TestSubscriber<Map.Entry<String, Boolean>> subscriber = new TestSubscriber<>();

        pocket.stream(Boolean.class).subscribe(subscriber);

        pocket.put(TEST_KEY, TEST_VALUE).blockingAwait();

        subscriber.assertNoValues();

        pocket.put(TEST_KEY, true).blockingAwait();
        pocket.put(TEST_KEY, false).blockingAwait();

        Assertions.assertThat(subscriber.valueCount()).isEqualTo(2);
    */
    }
}
