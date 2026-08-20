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
 * Thrown when an owner's telephone cannot be normalized into valid E.164 form — that is, a
 * leading '+' followed by 8 to 15 digits, after stripping spaces, dashes and brackets and
 * assuming the '+61' country code when none is given. The exception handler translates it to a
 * 400 Bad Request whose {@code errors} array names the offending {@code telephone} field.
 */
public class InvalidTelephoneException extends RuntimeException {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must form a valid E.164 number ('+' followed by 8 to 15 digits): " + telephone);
    }
}
