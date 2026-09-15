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
 * Thrown when an owner's postcode is present but is not four digits, or is out of range for
 * the region of the owner's city. Carries the rejected input so it can be logged.
 */
public class InvalidPostcodeException extends RuntimeException {

    /** Name of the offending field, used to build the error response. */
    public static final String FIELD = "postcode";

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
