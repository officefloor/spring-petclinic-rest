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

/**
 * Thrown when an owner's postcode is present but is not exactly 4 digits, or is out of the valid
 * range for the owner's city per the fixed region-to-range table (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). Handled as a 400 Bad Request by {@link ExceptionControllerAdvice}.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final transient Object rejectedValue;

    public InvalidPostcodeException(Object rejectedValue) {
        super("must be a 4-digit postcode valid for the owner's city");
        this.rejectedValue = rejectedValue;
    }

    public Object getRejectedValue() {
        return this.rejectedValue;
    }
}
