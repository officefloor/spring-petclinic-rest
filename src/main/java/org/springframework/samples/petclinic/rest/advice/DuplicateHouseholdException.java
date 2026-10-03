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

/**
 * Thrown when a request to create an owner would join an existing household (the deterministic
 * {@code householdId}, derived from the owner's normalized last name and postcode, already belongs
 * to another owner) without declaring {@code sharesHousehold}. Owners sharing a last name and
 * postcode are the same household, so a second such owner is a household duplicate unless it opts in
 * via {@code sharesHousehold}. Carries the offending household id so the
 * {@link ExceptionControllerAdvice} can report it to the client, which answers with a 409 Conflict.
 */
public class DuplicateHouseholdException extends RuntimeException {

    private final transient String householdId;

    public DuplicateHouseholdException(String householdId) {
        super("Household already in use: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
