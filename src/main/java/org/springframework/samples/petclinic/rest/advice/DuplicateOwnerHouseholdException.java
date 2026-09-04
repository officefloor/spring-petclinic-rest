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
 * Thrown when a request to create an owner shares a household - the same {@code lastName} and
 * {@code address}, compared case-insensitively with collapsed whitespace - with an existing owner,
 * and the request has not opted in with {@code sharesHousehold}. Carries the offending last name and
 * address so the {@link ExceptionControllerAdvice} can report the collision back to the client in a
 * {@code 409 Conflict} response.
 */
public class DuplicateOwnerHouseholdException extends DuplicateOwnerException {

    private static final String DETAIL = "An owner with the given last name and address already exists";

    private final String lastName;

    private final String address;

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Household already in use by another owner: " + lastName + " / " + address, DETAIL,
            List.of("lastName", "address"));
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
