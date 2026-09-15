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
 * Thrown when an owner is created with a normalized telephone that is already used by
 * another owner. Carries the offending value so it can be logged.
 */
public class DuplicateTelephoneException extends RuntimeException {

    /** Name of the offending field, used to build the error response. */
    public static final String FIELD = "telephone";

    public DuplicateTelephoneException(String telephone) {
        super("Telephone is already in use by another owner: " + telephone);
    }
}
