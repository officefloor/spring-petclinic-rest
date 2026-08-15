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
 * Raised when a create request supplies an owner whose last name and address match those of an
 * existing owner (compared case-insensitively with collapsed whitespace), and the request did not
 * opt in via {@code sharesHousehold}. Carries the conflicting values so the API can report the
 * rejection back to the client.
 */
public class DuplicateHouseholdException extends RuntimeException {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
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
