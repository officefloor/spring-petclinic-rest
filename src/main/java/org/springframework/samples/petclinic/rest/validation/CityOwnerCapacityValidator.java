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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.stereotype.Component;

/**
 * Enforces that a city cannot grow beyond {@value #MAX_OWNERS_PER_CITY} owners. The existing owners
 * in the city are counted case-insensitively via {@link CityOwnerCounter}; once that count reaches
 * the cap, no further owner may be created there.
 */
@Component
public class CityOwnerCapacityValidator {

    /** The maximum number of owners allowed in a single city. */
    public static final long MAX_OWNERS_PER_CITY = 50;

    private final CityOwnerCounter cityOwnerCounter;

    public CityOwnerCapacityValidator(CityOwnerCounter cityOwnerCounter) {
        this.cityOwnerCounter = cityOwnerCounter;
    }

    /**
     * Rejects an owner whose city already holds {@value #MAX_OWNERS_PER_CITY} or more owners.
     *
     * @param city the city of the owner being created
     * @throws CityOwnerCapacityExceededException if the city is already at capacity
     */
    public void validateHasCapacity(String city) {
        if (this.cityOwnerCounter.count(city) >= MAX_OWNERS_PER_CITY) {
            throw new CityOwnerCapacityExceededException(city, MAX_OWNERS_PER_CITY);
        }
    }
}
