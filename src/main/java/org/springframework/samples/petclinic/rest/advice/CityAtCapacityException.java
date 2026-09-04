/*
 * Copyright 2016 the original author or authors.
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

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Signals that a new owner's city already holds the maximum number of owners.
 * Mapped to HTTP 409 Conflict by {@link ExceptionControllerAdvice}.
 */
public class CityAtCapacityException extends RuntimeException {

    /** Maximum owners allowed per city; the next owner in a full city is rejected. */
    private static final int CITY_CAPACITY = 50;

    public CityAtCapacityException(String city) {
        super("City already at capacity: " + city);
    }

    /** Canonicalize a city so comparison ignores case and collapses whitespace runs. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Throw if {@code existingOwners} already contains {@value #CITY_CAPACITY} or more owners
     * whose normalized city matches {@code city}.
     */
    public static void rejectIfAtCapacity(String city, Collection<Owner> existingOwners) {
        String candidate = normalize(city);
        long count = existingOwners.stream()
            .filter(owner -> candidate.equals(normalize(owner.getCity())))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
