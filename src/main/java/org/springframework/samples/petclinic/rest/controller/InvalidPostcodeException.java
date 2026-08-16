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
 * Raised when a create request supplies a {@code postcode} that is present but not valid for
 * the owner's city: it falls outside the inclusive 4-digit range fixed for the city's region
 * (Sydney/NSW 2000-2099, Melbourne/VIC 3000-3099, Brisbane/QLD 4000-4099). Carries the
 * rejected value so the exception handler can report a 400 Bad Request naming the
 * {@code postcode} field. A city with no known region imposes no range and never raises this.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final String postcode;

    public InvalidPostcodeException(String postcode) {
        super("Invalid postcode for city: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
