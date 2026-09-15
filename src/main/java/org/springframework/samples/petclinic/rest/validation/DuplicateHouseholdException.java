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

package org.springframework.samples.petclinic.rest.validation;

import java.util.List;

/**
 * Thrown when an owner is created whose last name and address already belong to another
 * owner (compared case-insensitively with collapsed whitespace) and the request did not
 * opt in via {@code sharesHousehold}. Names the offending fields so the response can list
 * them.
 */
public class DuplicateHouseholdException extends RuntimeException {

    /** Names of the fields whose combination is already in use. */
    public static final List<String> FIELDS = List.of("lastName", "address");

    public DuplicateHouseholdException(String lastName, String address) {
        super("Last name and address are already in use by another owner: " + lastName + ", " + address);
    }
}
