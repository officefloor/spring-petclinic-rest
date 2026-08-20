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
 * Thrown when a request to create an owner carries the same last name and address as an
 * existing owner (compared case-insensitively with collapsed whitespace) and does not
 * opt in via the {@code sharesHousehold} flag. The exception handler translates it to a
 * 409 Conflict whose {@code errors} array names the offending {@code lastName} and
 * {@code address} fields.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Another owner already shares this household (lastName + address): "
            + lastName + " / " + address);
    }
}
