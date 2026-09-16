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
 * Decides whether the "capacity" warning applies to a city: it does once the city already holds
 * at least {@value #CAPACITY_WARNING_THRESHOLD} owners but has not yet reached the hard limit of
 * {@link CityOwnerCapacityValidator#MAX_OWNERS_PER_CITY}, signalling that the city is approaching
 * capacity. The owners in the city are counted case-insensitively via {@link CityOwnerCounter}, the
 * same accumulation path used to enforce the per-city cap in {@link CityOwnerCapacityValidator}.
 */
@Component
public class CityCapacityWarningEvaluator {

    /** The number of owners a city must reach before the capacity warning is raised. */
    public static final long CAPACITY_WARNING_THRESHOLD = 40;

    private final CityOwnerCounter cityOwnerCounter;

    public CityCapacityWarningEvaluator(CityOwnerCounter cityOwnerCounter) {
        this.cityOwnerCounter = cityOwnerCounter;
    }

    /**
     * @param city the city to evaluate
     * @return {@code true} when the city already holds between {@value #CAPACITY_WARNING_THRESHOLD}
     * and {@link CityOwnerCapacityValidator#MAX_OWNERS_PER_CITY} (exclusive) owners.
     */
    public boolean isWarranted(String city) {
        long count = this.cityOwnerCounter.count(city);
        return count >= CAPACITY_WARNING_THRESHOLD
            && count < CityOwnerCapacityValidator.MAX_OWNERS_PER_CITY;
    }
}
