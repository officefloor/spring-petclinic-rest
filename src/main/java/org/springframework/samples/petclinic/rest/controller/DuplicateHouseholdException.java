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
 * Signals that a new owner could not be created because another owner already belongs to the
 * same household (same last name and postcode, i.e. the same computed {@code householdId}).
 *
 * <p>Since the household is keyed on (last name, postcode), a second owner in an existing
 * household is a duplicate. Callers may register such an owner intentionally by setting
 * {@code sharesHousehold}, which bypasses this check. Handled as a 409 Conflict, reporting
 * {@code householdId} as the offending field.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String householdId) {
        super("Household already registered by another owner: " + householdId);
    }
}
