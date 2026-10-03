/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.service;

/**
 * Thrown when an attempt is made to create an {@code Owner} in a city that already contains the
 * maximum number of owners ({@value #MAX_OWNERS_PER_CITY}). Enforcing it in the service layer keeps
 * the capacity invariant in one place regardless of which caller initiates the create; the REST
 * layer maps this to a {@code 409 Conflict}.
 */
public class CityOwnerLimitExceededException extends RuntimeException {

    public static final int MAX_OWNERS_PER_CITY = 50;

    public CityOwnerLimitExceededException(String city) {
        super("The city '" + city + "' already contains the maximum of " + MAX_OWNERS_PER_CITY + " owners");
    }

}
