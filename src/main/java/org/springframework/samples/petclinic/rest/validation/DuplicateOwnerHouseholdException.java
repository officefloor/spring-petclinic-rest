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
 * Thrown when an owner is created whose last name and address already belong to another owner
 * (compared case-insensitively and ignoring whitespace differences) and the request did not opt
 * in via {@code sharesHousehold}. The REST layer reports this as a 409 Conflict.
 *
 * @see OwnerHouseholdUniquenessValidator
 */
public class DuplicateOwnerHouseholdException extends RuntimeException {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("An owner named '" + lastName + "' already lives at '" + address + "'");
    }
}
