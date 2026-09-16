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

/**
 * Thrown when an owner is created whose household — its (last name, postcode) derived
 * {@link HouseholdKey#idFor(String, String) household id} — already belongs to another owner and it
 * did not declare itself a member via {@code sharesHousehold}. The REST layer reports this as a 409
 * Conflict.
 *
 * @see HouseholdDuplicateValidator
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String householdId) {
        super("An owner in household '" + householdId + "' already exists");
    }
}
