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

/**
 * Simple pojo
 */
class Person {

    String firstName;
    String lastName;
    int age;

    Person(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Person)) {
            return false;
        }

        Person person = (Person) object;

        if (!firstName.equals(person.firstName)) {
            return false;
        }

        if (!lastName.equals(person.lastName)) {
            return false;
        }

        if (age != person.age) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = result * age + lastName.hashCode();
        return result;
    }
}
