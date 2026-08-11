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
 * Thrown when a supplied owner postcode is invalid: it is not 4 digits, or it falls outside the
 * range allowed for the region of the owner's city (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
 * A city with no known region accepts any 4-digit postcode, and an absent postcode is never
 * rejected. Carries the offending (raw) value so it can be reported to the client.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final String rejectedValue;

    public InvalidPostcodeException(String rejectedValue, String city) {
        super("Postcode '" + rejectedValue + "' is not valid for city '" + city + "'");
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
