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
 * Thrown when an owner is created that would join an existing household it has not declared
 * membership of. The household is keyed on the computed {@code householdId} (derived from the last
 * name and postcode), so an existing owner carrying the same {@code householdId} makes the new owner
 * a household duplicate. The create is rejected with a {@code 409 Conflict} unless the request opts
 * in via {@code sharesHousehold}.
 */
public class DuplicateHouseholdException extends RuntimeException {

    private final transient String householdId;

    public DuplicateHouseholdException(String householdId) {
        super("An owner in household " + householdId + " already exists");
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
