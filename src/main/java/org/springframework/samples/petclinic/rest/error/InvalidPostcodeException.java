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

package org.springframework.samples.petclinic.rest.error;

/**
 * Thrown when a submitted postcode is present but not a valid 4-digit code, or falls outside the
 * inclusive range permitted for the region of the owner's city. Carries the originally submitted
 * value so the REST layer can report what was rejected.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final String rejectedValue;

    public InvalidPostcodeException(String rejectedValue) {
        super("Postcode must be a 4-digit code valid for the owner's city, but was: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
