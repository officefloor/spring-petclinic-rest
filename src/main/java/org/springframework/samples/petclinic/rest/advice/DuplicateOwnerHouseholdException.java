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
 * Raised when an owner submitted to the create endpoint shares a household with an existing owner -
 * that is, another owner already has the same {@code lastName} and the same {@code address} (compared
 * case-insensitively with collapsed whitespace) - and the request did not opt in via
 * {@code sharesHousehold}. The {@code ExceptionControllerAdvice} renders this as a 409 Conflict
 * response.
 */
public class DuplicateOwnerHouseholdException extends RuntimeException {

    private final String lastName;

    private final String address;

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Owner already exists with the same last name and address: " + lastName + ", " + address);
        this.lastName = lastName;
        this.address = address;
    }

    /**
     * @return the canonical last name that collided with an existing owner
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * @return the canonical address that collided with an existing owner
     */
    public String getAddress() {
        return this.address;
    }
}
