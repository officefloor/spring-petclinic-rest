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
 * Thrown when a request to create an owner supplies a last name and address that, compared
 * case-insensitively with collapsed whitespace, already belong to another owner. Owners sharing a
 * last name and address are treated as the same household, so the {@link ExceptionControllerAdvice}
 * reports this back to the client as a 409 Conflict - unless the request opts in by setting
 * {@code sharesHousehold} to true.
 */
public class DuplicateOwnerHouseholdException extends RuntimeException {

    private final String lastName;

    private final String address;

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Owner already exists with last name '" + lastName + "' at address: " + address);
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
