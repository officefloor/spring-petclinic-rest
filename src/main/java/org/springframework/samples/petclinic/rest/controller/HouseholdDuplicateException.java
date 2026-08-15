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

package org.springframework.samples.petclinic.rest.controller;

/**
 * Raised when a create request supplies an owner that would join an existing household without
 * declaring it. The household is keyed on the computed {@code householdId} (the first 12 hex
 * characters of the SHA-256 digest of the owner's normalized last name and postcode), so any existing
 * owner sharing that identifier is already a member of the same household. Such a create is rejected
 * unless the request opts in via {@code sharesHousehold}, in which case the owner is created as a
 * declared household member. Carries the shared household id so the API can report the rejection.
 */
public class HouseholdDuplicateException extends RuntimeException {

    private final String householdId;

    public HouseholdDuplicateException(String householdId) {
        super("An owner already belongs to household '" + householdId + "'");
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
