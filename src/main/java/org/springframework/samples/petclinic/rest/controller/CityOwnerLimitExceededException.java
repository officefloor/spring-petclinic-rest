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

package org.springframework.samples.petclinic.rest.controller;

/**
 * Raised when a create request supplies an owner whose city (compared case-insensitively) already
 * contains the maximum permitted number of owners. Carries the conflicting city and the limit so the
 * API can report the rejection back to the client.
 */
public class CityOwnerLimitExceededException extends RuntimeException {

    private final String city;

    private final int limit;

    public CityOwnerLimitExceededException(String city, int limit) {
        super("City '" + city + "' already contains the maximum of " + limit + " owners");
        this.city = city;
        this.limit = limit;
    }

    public String getCity() {
        return this.city;
    }

    public int getLimit() {
        return this.limit;
    }
}
