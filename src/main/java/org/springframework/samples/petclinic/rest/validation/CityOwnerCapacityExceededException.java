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
 * Thrown when an owner is created in a city that has already reached its maximum number of owners.
 * The REST layer reports this as a 409 Conflict.
 *
 * @see CityOwnerCapacityValidator
 */
public class CityOwnerCapacityExceededException extends RuntimeException {

    public CityOwnerCapacityExceededException(String city, long maxOwners) {
        super("City '" + city + "' has reached its capacity of " + maxOwners + " owners");
    }
}
