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
package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Enforces the per-city owner cap: a city is full once it already holds
 * {@value #MAX_OWNERS_PER_CITY} owners, comparing city names case-insensitively.
 */
public final class CityCapacity {

    public static final int MAX_OWNERS_PER_CITY = 50;

    private CityCapacity() {
    }

    public static boolean isFull(Collection<Owner> existing, Owner owner) {
        String city = owner.getCity();
        long inCity = existing.stream().filter(o -> city.equalsIgnoreCase(o.getCity())).count();
        return inCity >= MAX_OWNERS_PER_CITY;
    }
}
