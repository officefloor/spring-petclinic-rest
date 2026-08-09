/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.advice;

/**
 * Thrown when a request to create an owner names a city that already contains the maximum number of
 * owners (compared case-insensitively with collapsed whitespace). A city is capped at 50 owners, so
 * once it is full any further create for that city is reported to the caller as an HTTP 409.
 */
public class OwnerCityCapacityExceededException extends RuntimeException {

    private final String city;

    private final int capacity;

    public OwnerCityCapacityExceededException(String city, int capacity) {
        super("City already contains the maximum of " + capacity + " owners: " + city);
        this.city = city;
        this.capacity = capacity;
    }

    public String getCity() {
        return this.city;
    }

    public int getCapacity() {
        return this.capacity;
    }
}
