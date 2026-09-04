/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.Collection;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}:
 * the upper-cased first three letters of the city and of the last name, and a
 * per-city 4-digit zero-padded sequence one greater than the number of owners
 * already in that city.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * @param owner          the owner being coded (its city and last name are used)
     * @param existingOwners the current owners (this one excluded)
     * @return the formatted customer code, e.g. {@code SYD-SMI-0007}
     */
    public static String of(Owner owner, Collection<Owner> existingOwners) {
        long inCity = existingOwners.stream()
            .filter(o -> o.getCity() != null && o.getCity().equalsIgnoreCase(owner.getCity()))
            .count();
        return String.format("%s-%s-%04d", prefix3(owner.getCity()), prefix3(owner.getLastName()), inCity + 1);
    }

    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
