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

package org.springframework.samples.petclinic.service;

/**
 * Thrown when a new owner is created whose city (compared case-insensitively) already contains
 * {@link org.springframework.samples.petclinic.service.ClinicServiceImpl#MAX_OWNERS_PER_CITY} or
 * more owners. Mapped to an HTTP 409 Conflict response by the web layer.
 */
public class CityAtCapacityException extends RuntimeException {

    public CityAtCapacityException(String city) {
        super("The city '" + city + "' already contains the maximum number of owners");
    }

}
