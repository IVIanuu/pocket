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

import android.support.annotation.CheckResult
import com.ivianuu.pocket.Pocket

/**
 * Reads the value for the key
 */
@CheckResult
inline fun <reified T> Pocket.get(key: String) = this.get(key, T::class.java)

/**
 * Reads the value for the key
 * if no value found it will return the default value
 */
@CheckResult
inline fun <reified T> Pocket.get(key: String, defaultValue: T) = this.get(key, defaultValue, T::class.java)

/**
 * Reads the value for the key
 * if no value found it will return the default value
 */
@CheckResult
inline fun <reified T> Pocket.getOptional(key: String) = this.getOptional(key, T::class.java)

/**
 * Returns a map of all all keys with the provided value type
 */
@CheckResult
inline fun <reified T> Pocket.getAll() = this.getAll(T::class.java)

/**
 * Emits on key changes
 */
@CheckResult
inline fun <reified T> Pocket.stream() = this.stream(T::class.java)

/**
 * Emits on value changes and on first subscribe
 */
@CheckResult
inline fun <reified T> Pocket.stream(key: String) = this.stream(key, T::class.java)