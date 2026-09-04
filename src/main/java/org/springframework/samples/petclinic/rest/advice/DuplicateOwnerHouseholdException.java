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

import java.util.List;

/**
 * Thrown when a request to create an owner would join an existing household - another owner already
 * carries the same {@code householdId}, derived from the owner's last name and postcode - without
 * opting in with {@code sharesHousehold}. Because the household is keyed on (last name, postcode), a
 * second owner with the same last name and postcode denotes the same household and is rejected as a
 * {@code 409 Conflict} unless the request declares itself a household member. Carries the offending
 * household id so the {@link ExceptionControllerAdvice} can report the collision back to the client.
 */
public class DuplicateOwnerHouseholdException extends DuplicateOwnerException {

    private static final String DETAIL =
        "An owner already belongs to this household; set 'sharesHousehold' to join it";

    private final String householdId;

    public DuplicateOwnerHouseholdException(String householdId) {
        super("Household already in use by another owner: " + householdId, DETAIL,
            List.of("lastName", "postcode"));
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
