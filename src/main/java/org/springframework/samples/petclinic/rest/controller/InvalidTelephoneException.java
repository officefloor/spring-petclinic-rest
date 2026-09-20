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
 * Thrown when a submitted telephone cannot be reduced to a valid E.164 number: either it
 * does not have a leading {@code '+'} followed by 8 to 15 digits, or its national number is
 * the wrong length for its country calling code. Handled as {@code 400 Bad Request}.
 */
public class InvalidTelephoneException extends RuntimeException {

    private InvalidTelephoneException(String message) {
        super(message);
    }

    /** The value cannot form an E.164 number of 8 to 15 digits after the {@code '+'}. */
    public static InvalidTelephoneException notE164(String rejectedValue) {
        return new InvalidTelephoneException(
            "telephone must form a valid E.164 number with 8 to 15 digits after the '+' (rejected value: "
                + rejectedValue + ")");
    }

    /** The national number is the wrong length for its country calling code. */
    public static InvalidTelephoneException wrongNationalLength(String rejectedValue, String callingCode,
            int required, int actual) {
        return new InvalidTelephoneException("telephone for country code '+" + callingCode + "' must have "
            + required + " national digits but had " + actual + " (rejected value: " + rejectedValue + ")");
    }
}
