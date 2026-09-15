/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.rest;

import org.springframework.samples.petclinic.rest.validation.CityOwnerLimitException;
import org.springframework.stereotype.Component;

/**
 * Decides whether an owner being created should carry a capacity warning. The warning flags
 * that the owner's city is approaching the per-city capacity limit: it already holds between
 * 40 and 49 owners. At the hard limit ({@link CityOwnerLimitException#MAX_OWNERS_PER_CITY})
 * creation is rejected instead, so the warning covers only the band just below it.
 */
@Component
public class CityCapacityWarningEvaluator {

    /** Owner count at or above which a city is flagged as approaching its capacity limit. */
    public static final long CAPACITY_WARNING_THRESHOLD = 40;

    /**
     * Whether an owner joining a city that already holds {@code ownersInCity} owners should be
     * flagged as approaching the city capacity limit.
     *
     * @param ownersInCity owners already registered in the city, excluding the new one
     * @return {@code true} when the city already holds at least
     *         {@link #CAPACITY_WARNING_THRESHOLD} owners but is still below the hard limit
     *         {@link CityOwnerLimitException#MAX_OWNERS_PER_CITY}, otherwise {@code false}
     */
    public boolean isApproachingCapacity(long ownersInCity) {
        return ownersInCity >= CAPACITY_WARNING_THRESHOLD
            && ownersInCity < CityOwnerLimitException.MAX_OWNERS_PER_CITY;
    }
}
