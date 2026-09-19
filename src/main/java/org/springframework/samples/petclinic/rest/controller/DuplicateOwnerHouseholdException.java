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
 * Signals that a new owner could not be created because another owner already shares
 * the same last name and address (compared case-insensitively with collapsed
 * whitespace) and the request did not opt in to a shared household.
 *
 * <p>Handled as a 409 Conflict, reporting {@code lastName} and {@code address} as the
 * offending fields.
 */
public class DuplicateOwnerHouseholdException extends RuntimeException {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: "
            + lastName + ", " + address);
    }
}
