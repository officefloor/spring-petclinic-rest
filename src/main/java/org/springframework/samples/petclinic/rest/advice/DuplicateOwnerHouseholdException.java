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
 * Thrown when an owner is created whose last name and address (compared case-insensitively
 * with collapsed whitespace) already match another owner - i.e. they would share a household -
 * without the request opting in via the {@code sharesHousehold} flag.
 * <p>
 * The exception handler reports this as a 409 Conflict, since the request collides with
 * the current state of an existing resource.
 */
public class DuplicateOwnerHouseholdException extends RuntimeException {

    private final String lastName;

    private final String address;

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' at address '" + address + "' already exists");
        this.lastName = lastName;
        this.address = address;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getAddress() {
        return this.address;
    }
}
