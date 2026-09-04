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
 * Thrown when a request to create an owner would exceed a city's capacity - that is, the city already
 * holds the maximum permitted number of owners. Unlike the {@link DuplicateOwnerException} family this
 * is a capacity limit rather than a uniqueness collision, but it is likewise reported to the client as
 * a {@code 409 Conflict}. Carries the offending city and the client-facing detail so the
 * {@link ExceptionControllerAdvice} can render the response.
 */
public class OwnerCityAtCapacityException extends RuntimeException {

    private static final String DETAIL = "The owner's city has reached its maximum number of owners";

    private final String city;

    public OwnerCityAtCapacityException(String city) {
        super("City already at owner capacity: " + city);
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }

    public String getDetail() {
        return DETAIL;
    }

    public List<String> getErrorFields() {
        return List.of("city");
    }
}
