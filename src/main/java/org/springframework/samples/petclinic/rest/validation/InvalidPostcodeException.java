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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Raised when a supplied postcode is out of range for its city's region (see
 * {@link PostcodeValidator}), so the client can be told the value is invalid.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final String postcode;

    public InvalidPostcodeException(String postcode, String city) {
        super("Postcode " + postcode + " is not valid for city: " + city);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
