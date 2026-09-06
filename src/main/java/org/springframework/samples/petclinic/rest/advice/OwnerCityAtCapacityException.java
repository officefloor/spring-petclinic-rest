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
 * Raised when an owner submitted to the create endpoint would be placed in a city that already
 * contains the maximum number of owners (50 or more). The {@code ExceptionControllerAdvice} renders
 * this as a 409 Conflict response.
 */
public class OwnerCityAtCapacityException extends RuntimeException {

    private final String city;

    public OwnerCityAtCapacityException(String city) {
        super("City is at capacity: " + city);
        this.city = city;
    }

    /**
     * @return the city that has already reached its owner capacity
     */
    public String getCity() {
        return this.city;
    }
}
