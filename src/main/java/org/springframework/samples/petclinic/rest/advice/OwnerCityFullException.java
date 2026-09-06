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
 * Thrown when a request to create an owner names a city that already contains 50 or more owners.
 * A city is considered full at that point, so the {@link ExceptionControllerAdvice} reports this
 * back to the client as a 409 Conflict.
 */
public class OwnerCityFullException extends RuntimeException {

    private final String city;

    public OwnerCityFullException(String city) {
        super("City already has the maximum number of owners: " + city);
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }

}
