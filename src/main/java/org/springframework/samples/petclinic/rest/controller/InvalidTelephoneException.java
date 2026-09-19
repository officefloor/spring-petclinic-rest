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
 * Signals that an owner request supplied a telephone number that cannot be converted into
 * a valid E.164 form (8 to 15 digits after the {@code +}).
 *
 * <p>Handled as a 400 Bad Request, reporting {@code telephone} as the offending field.
 */
public class InvalidTelephoneException extends RuntimeException {

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone must form a valid E.164 number (8 to 15 digits after the '+'): " + rejectedValue);
    }
}
