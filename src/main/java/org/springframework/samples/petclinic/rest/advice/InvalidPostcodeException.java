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
 * Thrown when a request to create an owner supplies a {@code postcode} that is out of range for
 * the owner's city per the fixed region table (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
 * Carries the offending value so the exception handler can report it.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final String rejectedValue;

    public InvalidPostcodeException(String rejectedValue) {
        super("Postcode is not valid for the owner's city region");
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
