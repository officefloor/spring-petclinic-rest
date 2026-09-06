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
 * Raised when an owner submitted to the create endpoint carries a 4-digit {@code postcode} that is
 * out of range for the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). The
 * malformed (non 4-digit) case is already rejected by Bean Validation's {@code @Pattern} constraint;
 * this closes the gap for a well-formed postcode that falls outside the city region's range. The
 * {@code ExceptionControllerAdvice} renders it as a 400 response naming the {@code postcode} field.
 */
public class InvalidOwnerPostcodeException extends RuntimeException {

    private final String postcode;

    private final String city;

    public InvalidOwnerPostcodeException(String postcode, String city) {
        super("Postcode '" + postcode + "' is out of range for city '" + city + "'");
        this.postcode = postcode;
        this.city = city;
    }

    /**
     * @return the rejected postcode
     */
    public String getPostcode() {
        return this.postcode;
    }

    /**
     * @return the city whose region the postcode was out of range for
     */
    public String getCity() {
        return this.city;
    }
}
